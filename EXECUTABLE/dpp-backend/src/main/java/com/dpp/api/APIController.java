package com.dpp.api;

import java.time.Instant;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dpp.MongoDppTemplate;
import com.dpp.util.ValidateDPP;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
public ResponseEntity<ObjectNode> deleteDppEntry(@PathVariable String dppId) {
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


}