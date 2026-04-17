package com.dpp.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dpp.MongoDppTemplate;
import com.dpp.util.ValidateDPP;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.type.TypeReference;

import com.mongodb.client.result.UpdateResult;

@RestController
@RequestMapping
public class APIController {

    private static final Logger logger = LoggerFactory.getLogger(APIController.class);

    private final ObjectMapper mapper;
    private final MongoTemplate mongoTemplate; // Using MongoTemplate for Upsert logic

    /**
     * Constructor injection for the ObjectMapper and MongoTemplate.
     * Spring Boot automatically provides these beans.
     */
    public APIController(ObjectMapper mapper, MongoTemplate mongoTemplate) {
        this.mapper = mapper;
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping("/health")
    public ResponseEntity<ObjectNode> health() {
        ObjectNode node = mapper.createObjectNode();
        node.put("status", "UP");
        return ResponseEntity.ok(node);
    }

    /**
     * Processes a DPP request.
     * 1. Extracts the Shell ID.
     * 2. Finds the shell in MongoDB or prepares to create it.
     * 3. Appends the provided DPP entry to the 'dpps' array.
     */
    @PostMapping("/dpps")
    public ResponseEntity<ObjectNode> createDpp(@RequestBody JsonNode dpp) {
        ObjectNode response = mapper.createObjectNode();

        // Validate the structure using the utility class
        if (!ValidateDPP.validateJsonTillFirstEntry(dpp)) {
            logger.error("Validation failed for incoming DPP");
            return ResponseEntity.badRequest().body(response.put("error", "invalid Dpp structure"));
        }

        try {
            // 1. Extract the Shell ID to use as the Document ID (_id)
            String shellId = dpp.get("shell").get("id").asText();

            // 2. Extract the first DPP entry from the incoming JSON array
            JsonNode dppEntry = dpp.get("shell").get("dpps").get(0);

            // get The current time construct the dppId
            String timeStamp = String.valueOf(Instant.now().toEpochMilli());
            String dppId = dppEntry.get("productId").asText() + timeStamp.toString();

            // Map the JsonNode to our MongoDppInit POJO
            MongoDppTemplate dppContent = mapper.treeToValue(dppEntry, MongoDppTemplate.class);

            // append dppId and createdAt
            dppContent.setCreatedAt(timeStamp);
            dppContent.setDppId(dppId);

            // 3. Define the query to find the Shell by its ID
            Query query = new Query(Criteria.where("_id").is(shellId));

            // 4. Define the update logic:
            // $push adds the entry to the 'dpps' array.
            // If the document or the array doesn't exist, MongoDB creates them.
            Update update = new Update().push("dpps", dppContent);

            // 5. Execute Upsert: Find and Update, or Insert if not found
            mongoTemplate.upsert(query, update, "dpp-repo");

            logger.info("Successfully updated/created shell ID: {}", shellId);

            response.put("status", "success");
            response.put("dppId", dppId);
            return ResponseEntity.status(201).body(response);

        } catch (Exception e) {
            logger.error("Error during MongoDB operation", e);
            response.put("error", "Internal Server Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/dpps/{dppId}")
    public ResponseEntity<ObjectNode> readDppById(@PathVariable String dppId) {
        ObjectNode response = mapper.createObjectNode();

        try {
            Aggregation aggregation = Aggregation.newAggregation(
                    // We match against _id because your dppId is marked with @Id
                    Aggregation.match(Criteria.where("dpps._id").is(dppId)),
                    Aggregation.unwind("dpps"),
                    Aggregation.match(Criteria.where("dpps._id").is(dppId)),
                    Aggregation.replaceRoot("dpps"));

            List<org.bson.Document> results = mongoTemplate.aggregate(
                    aggregation, "dpp-repo", org.bson.Document.class).getMappedResults();

            if (results.isEmpty()) {
                response.put("status", "failure");
                response.put("message", "DPP not found");
                return ResponseEntity.status(404).body(response);
            }

            // Use the converter to transform the BSON Document into your POJO [cite: 136]
            MongoDppTemplate dpp = mongoTemplate.getConverter().read(
                    MongoDppTemplate.class,
                    results.get(0));

            response.put("status", "success");
            response.putPOJO("dpp", dpp);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving DPP: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }


    /*
        find all dpps matchin a productId and return the last result.
        WARNING: assumption is that the latest entry has the newest version
     */
    @GetMapping("/dppsByProductId/{productId}")
    public ResponseEntity<ObjectNode> readDppByProductId(@PathVariable String productId) {
        ObjectNode response = mapper.createObjectNode();

        try {
            Aggregation aggregation = Aggregation.newAggregation(
                    // We match against _id because your dppId is marked with @Id
                    Aggregation.match(Criteria.where("dpps.productId").is(productId)),
                    Aggregation.unwind("dpps"),
                    Aggregation.match(Criteria.where("dpps.productId").is(productId)),
                    Aggregation.replaceRoot("dpps"));

            List<org.bson.Document> results = mongoTemplate.aggregate(
                    aggregation, "dpp-repo", org.bson.Document.class).getMappedResults();

            if (results.isEmpty()) {
                response.put("status", "failure");
                response.put("message", "DPP not found");
                return ResponseEntity.status(404).body(response);
            }

            // Use the converter to transform the BSON Document into your POJO [cite: 136]
            MongoDppTemplate dpp = mongoTemplate.getConverter().read(
                    MongoDppTemplate.class,
                    results.get(results.size()-1));

            response.put("status", "success");
            response.putPOJO("dpp", dpp);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving DPP: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

 
    
    @GetMapping("/dppsByProductIdAndDate/{productId}")
    public ResponseEntity<ObjectNode> readDppByProductIdAndDate(
        @PathVariable String productId,
        @RequestParam String timeStamp
    ) {
        ObjectNode response = mapper.createObjectNode();

        try {
            Aggregation aggregation = Aggregation.newAggregation(
                    // We match against _id because your dppId is marked with @Id
                    Aggregation.match(Criteria.where("dpps.productId").is(productId)
                                        .and("dpps.createdAt").is(timeStamp)),
                    Aggregation.unwind("dpps"),
                    Aggregation.match(Criteria.where("dpps.productId").is(productId)
                                        .and("dpps.createdAt").is(timeStamp)),
                    Aggregation.replaceRoot("dpps"));

            List<org.bson.Document> results = mongoTemplate.aggregate(
                    aggregation, "dpp-repo", org.bson.Document.class).getMappedResults();

            if (results.isEmpty()) {
                response.put("status", "failure");
                response.put("message", "DPP not found");
                return ResponseEntity.status(404).body(response);
            }

            // Use the converter to transform the BSON Document into your POJO [cite: 136]
            MongoDppTemplate dpp = mongoTemplate.getConverter().read(
                    MongoDppTemplate.class,
                    results.get(0));

            response.put("status", "success");
            response.putPOJO("dpp", dpp);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving DPP: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/dpps/{dppId}")
    public ResponseEntity<ObjectNode> deleteDpp(@PathVariable String dppId) {
        ObjectNode response = mapper.createObjectNode();

        try {
            // 1. Define the query to find the shell containing the specific dppId [cite: 154]
            // Note: We use "dpps._id" if your dppId is marked with @Id 
            Query query = new Query(Criteria.where("dpps._id").is(dppId));

            // 2. Define the update logic using $pull to remove the object from the array 
            Update update = new Update().pull("dpps", new org.bson.Document("_id", dppId));

            // 3. Execute the update [cite: 150]
            UpdateResult result = mongoTemplate.updateFirst(query, update, "dpp-repo");

            if (result.getModifiedCount() == 0) {
                response.put("status", "failure");
                response.put("message", "No entry found to delete");
                return ResponseEntity.status(404).body(response);
            }

            response.put("status", "success");
            response.put("message", "DPP entry deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/dppIdsByProductIds")
    public ResponseEntity<ObjectNode> getDppIdsByProductIds(@RequestBody List<String> productIds) {
        ObjectNode response = mapper.createObjectNode();

        try {
            // 1) Aggregate: find DPPs whose productId is in the given list
            Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("dpps.productId").in(productIds)),
                Aggregation.unwind("dpps"),
                Aggregation.match(Criteria.where("dpps.productId").in(productIds)),
                Aggregation.project()
                    .and("dpps.productId").as("productId")
                    .and("dpps._id").as("dppId")
            );

            // 2) Execute aggregation and get raw MongoDB documents
            List<org.bson.Document> results = mongoTemplate
                .aggregate(aggregation, "dpp-repo", org.bson.Document.class)
                .getMappedResults();

            // 3) Group results by productId: { productId: [ { dppId: "..." }, ... ] }
            Map<String, List<ObjectNode>> grouped = new HashMap<>();
            for (org.bson.Document doc : results) {
                String productId = doc.getString("productId");
                String dppId = doc.getString("dppId");
                ObjectNode dpp = mapper.createObjectNode().put("dppId", dppId);
                grouped.computeIfAbsent(productId, k -> new ArrayList<>()).add(dpp);
            }

            // 4) Build JSON response
            ObjectNode groupedResults = mapper.convertValue(grouped, ObjectNode.class);
            response.put("status", "success");
            response.set("results", groupedResults);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving DPP IDs: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/dpps/{dppId}")
    public ResponseEntity<ObjectNode> updateDpp(
        @PathVariable String dppId,
        @RequestBody JsonNode updateData
    ) {
        ObjectNode response = mapper.createObjectNode();

        try {
            // 1) Find the existing DPP document inside the shell
            Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("dpps._id").is(dppId)),
                Aggregation.unwind("dpps"),
                Aggregation.match(Criteria.where("dpps._id").is(dppId)),
                Aggregation.replaceRoot("dpps")
            );

            List<org.bson.Document> oldDppDoc = mongoTemplate
                .aggregate(agg, "dpp-repo", org.bson.Document.class)
                .getMappedResults();

            if (oldDppDoc.isEmpty()) {
                response.put("status", "failure");
                response.put("message", "DPP not found");
                return ResponseEntity.status(404).body(response);
            }

            org.bson.Document oldDpp = oldDppDoc.get(0);
            String productId = oldDpp.getString("productId");

            // 2) Find the parent shell that contains this DPP
            Query shellQuery = new Query(Criteria.where("dpps._id").is(dppId));
            org.bson.Document shellDoc = mongoTemplate.findOne(
                shellQuery,
                org.bson.Document.class,
                "dpp-repo"
            );

            if (shellDoc == null) {
                response.put("status", "failure");
                response.put("message", "Shell for DPP not found");
                return ResponseEntity.status(404).body(response);
            }

            String shellId = shellDoc.getString("_id");

            // 3) Build the new DPP version
            String newTimestamp = String.valueOf(Instant.now().toEpochMilli());
            String newDppId = productId + newTimestamp;

            MongoDppTemplate newDpp = new MongoDppTemplate();
            newDpp.setDppId(newDppId);
            newDpp.setProductId(productId);
            newDpp.setCreatedAt(newTimestamp);

            // Preserve or update fields from the request
            if (updateData.has("version")) {
                newDpp.setVersion(updateData.get("version").asText());
            } else {
                newDpp.setVersion(oldDpp.getString("version"));
            }

            if (updateData.has("submodels")) {
                List<MongoDppTemplate.Submodels> submodels = mapper.convertValue(
                    updateData.get("submodels"),
                    new TypeReference<List<MongoDppTemplate.Submodels>>() {}
                );
                newDpp.setSubmodels(submodels);
            } else {
                newDpp.setSubmodels(null);
            }

            // 4) Remove the old DPP from the shell’s dpps array
            Update deleteUpdate = new Update().pull("dpps", new org.bson.Document("_id", dppId));
            UpdateResult deleteResult = mongoTemplate.updateFirst(
                shellQuery,
                deleteUpdate,
                "dpp-repo"
            );

            if (deleteResult.getModifiedCount() == 0) {
                response.put("status", "warning");
                response.put("message", "No DPP removed (already gone or shell mismatch)");
            }

            // 5) Insert the new DPP into the same shell
            Query insertQuery = new Query(Criteria.where("_id").is(shellId));
            Update insertUpdate = new Update().push("dpps", newDpp);
            UpdateResult insertResult = mongoTemplate.updateFirst(
                insertQuery,
                insertUpdate,
                "dpp-repo"
            );

            if (insertResult.getModifiedCount() == 0) {
                response.put("status", "failure");
                response.put("message", "No shell found to insert new DPP");
                return ResponseEntity.status(500).body(response);
            }

            // 6) Return success
            response.put("status", "success");
            response.put("message", "DPP updated (new version created)");
            response.put("newDppId", newDppId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Update error: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}