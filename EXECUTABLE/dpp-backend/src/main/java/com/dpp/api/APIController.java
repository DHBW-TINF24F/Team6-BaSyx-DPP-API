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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import com.dpp.MongoDppTemplate;
import com.dpp.MongoDppTemplate.Submodels;
import com.dpp.util.APIUtilsDPP;
import com.dpp.util.Base64DPP;
import com.dpp.util.ValidateDPP;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.result.UpdateResult;

@RestController
@RequestMapping
public class APIController {

    private static final Logger logger = LoggerFactory.getLogger(APIController.class);

    private final ObjectMapper mapper;
    private final RestClient restClient = RestClient.create();
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
     * 3. Calls external AAS registry to extract and filter submodels.
     * 4. Appends the provided DPP entry to the 'dpps' array.
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
            String dppId = dppEntry.get("productId").asText() + timeStamp;
            dppId = Base64DPP.ensureEncoding(dppId);

            String aasIdentifier = Base64DPP.ensureEncoding(dppEntry.get("productId").asText());

            List<MongoDppTemplate.Submodels> filteredSubmodels = new ArrayList<>();
            try {
                String externalApiBase = System.getenv("EXTERNAL_AAS_API_URL");
if (externalApiBase == null || externalApiBase.isEmpty()) {
    externalApiBase = "http://localhost:8081";
}

String externalUrl = externalApiBase + "/shells/" + aasIdentifier + "/submodel-refs";

                // Using RestClient (Blocking/Synchronous)
                JsonNode externalPayload = restClient.get()
                        .uri(externalUrl)
                        .retrieve()
                        .body(JsonNode.class);

                logger.info("External API call successful for AAS: {}", aasIdentifier);

                if (externalPayload != null && externalPayload.has("result")) {
                    filteredSubmodels = APIUtilsDPP.extractAndFilterSubmodels(externalPayload.get("result"));
                }

            } catch (Exception apiEx) {
                logger.warn("External API call to :8081 failed: {}", apiEx.getMessage());
            }

            // Map the JsonNode to our MongoDppInit POJO
            MongoDppTemplate dppContent = mapper.treeToValue(dppEntry, MongoDppTemplate.class);

            // append dppId, createdAt, and enriched submodels
            dppContent.setCreatedAt(timeStamp);
            dppContent.setDppId(dppId);
            dppContent.setSubmodels(filteredSubmodels);

            // ensure the productId is stored in base64 format
            dppContent.setProductId(Base64DPP.ensureEncoding(dppContent.getProductId()));

            // 3. Define the query to find the Shell by its ID
            Query query = new Query(Criteria.where("_id").is(shellId));

            // 4. Define the update logic:
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

            MongoDppTemplate dpp = mongoTemplate.getConverter().read(
                    MongoDppTemplate.class,
                    results.get(0));

            ObjectNode collctedSubmodels = APIUtilsDPP.collectSubmodelData(mapper, dpp, restClient, logger);

            response.put("status", "success");
            response.putPOJO("dpp", dpp);
            response.putPOJO("submodels_values", collctedSubmodels);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving DPP: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /*
     * ellementId -> submodelId
     * 
     * 1. get submodels from dpp
     * 2. search every submodel for the ellementId
     * 3. return all value of submodel
     */

    @GetMapping("/dpps/{dppId}/collections/{elementId}")
    public ResponseEntity<ObjectNode> readElementCollection(
            @PathVariable String dppId,
            @PathVariable String elementId) {
        ObjectNode response = mapper.createObjectNode();

        elementId = Base64DPP.ensureEncoding(elementId);

        try {

            MongoDppTemplate dpp = APIUtilsDPP.getDppByAggregatoin(Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("dpps._id").is(dppId)),
                    Aggregation.unwind("dpps"),
                    Aggregation.match(Criteria.where("dpps._id").is(dppId)),
                    Aggregation.replaceRoot("dpps")), mongoTemplate);

            for (Submodels submodel : dpp.getSubmodels()) {
                String submodelBase64 = Base64DPP.ensureEncoding(submodel.getReference());

                if (submodelBase64.equals(elementId)) {

                    try {
                        String externalApiBase = System.getenv("EXTERNAL_AAS_API_URL");
if (externalApiBase == null || externalApiBase.isEmpty()) {
    externalApiBase = "http://localhost:8081";
}

String externalUrl = externalApiBase + "/shells/" + submodelBase64 + "/submodel-refs";

                        // Using RestClient (Blocking/Synchronous)
                        JsonNode externalPayload = restClient.get()
                                .uri(externalUrl)
                                .retrieve()
                                .body(JsonNode.class);

                        logger.info("External API call successful for submodel: {}", submodel);

                        if (externalPayload != null && externalPayload.has("result")) {
                            response.put("status", "success");
                            response.putPOJO("payload", externalPayload.get("result"));
                            return ResponseEntity.ok(response);
                        }

                    } catch (Exception apiEx) {
                        logger.warn("External API call to :8081 failed: {}", apiEx.getMessage());
                        response.put("status", "failure");
                        return ResponseEntity.status(500).body(response);
                    }

                }
            }
            response.put("status", "failure");
            return ResponseEntity.status(500).body(response);

        } catch (Exception e) {
            logger.error("Error retrieving DPP: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PatchMapping("/dpps/{dppId}/collections/{elementId}")
    public ResponseEntity<ObjectNode> updateDataElementCollection(
            @PathVariable String dppId,
            @PathVariable String elementId,
            @RequestBody JsonNode body) {
        ObjectNode response = mapper.createObjectNode();
        elementId = Base64DPP.ensureEncoding(elementId);

        try {
            MongoDppTemplate dpp = APIUtilsDPP.getDppById(dppId, mongoTemplate);

            for (Submodels submodel : dpp.getSubmodels()) {
                String submodelBase64 = Base64DPP.ensureEncoding(submodel.getReference());
                if (submodelBase64.equals(elementId)) {

                    try {
                        String externalApiBase = System.getenv("EXTERNAL_AAS_API_URL");
if (externalApiBase == null || externalApiBase.isEmpty()) {
    externalApiBase = "http://localhost:8081";
}

String externalUrl = externalApiBase + "/shells/" + submodelBase64 + "/submodel-refs";

                        JsonNode externalPayload = restClient.patch()
                                .uri(externalUrl)
                                .contentType(MediaType.APPLICATION_JSON) // Specify JSON content type
                                .body(body) // Pass the 'body' variable here
                                .retrieve()
                                .body(JsonNode.class);

                        logger.info("External API call successful for submodel: {}", submodel);

                        if (externalPayload != null && externalPayload.has("result")) {
                            response.put("status", "success");
                            return ResponseEntity.ok(response);
                        }

                    } catch (Exception apiEx) {
                        logger.warn("External API call to :8081 failed: {}", apiEx.getMessage());
                        response.put("status", "failure");
                        return ResponseEntity.status(500).body(response);
                    }

                }
            }

        } catch (Exception e) {
            response.put("status", "failure");
            return ResponseEntity.status(500).body(response);
        }
        response.put("status", "success");
        return ResponseEntity.status(200).body(response);
    }

    @GetMapping("/dppsByProductId/{productId}")
    public ResponseEntity<ObjectNode> readDppByProductId(@PathVariable String productId) {
        ObjectNode response = mapper.createObjectNode();

        try {
            Aggregation aggregation = Aggregation.newAggregation(
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

            MongoDppTemplate dpp = mongoTemplate.getConverter().read(
                    MongoDppTemplate.class,
                    results.get(results.size() - 1));

            ObjectNode submodels = APIUtilsDPP.collectSubmodelData(mapper, dpp, restClient, logger);

            response.put("status", "success");
            response.putPOJO("dpp", dpp);
            response.putPOJO("submodels_values", submodels);
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
            @RequestParam String timeStamp) {
        ObjectNode response = mapper.createObjectNode();

        try {
            Aggregation aggregation = Aggregation.newAggregation(
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
            Query query = new Query(Criteria.where("dpps._id").is(dppId));
            Update update = new Update().pull("dpps", new org.bson.Document("_id", dppId));
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

    @PostMapping("/dppsByProductIds")
    public ResponseEntity<ObjectNode> getDppIdsByProductIds(@RequestBody List<String> productIds) {
        ObjectNode response = mapper.createObjectNode();

        try {
            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("dpps.productId").in(productIds)),
                    Aggregation.unwind("dpps"),
                    Aggregation.match(Criteria.where("dpps.productId").in(productIds)),
                    Aggregation.project()
                            .and("dpps.productId").as("productId")
                            .and("dpps._id").as("dppId"));

            List<org.bson.Document> results = mongoTemplate
                    .aggregate(aggregation, "dpp-repo", org.bson.Document.class)
                    .getMappedResults();

            Map<String, List<ObjectNode>> grouped = new HashMap<>();
            for (org.bson.Document doc : results) {
                String productId = doc.getString("productId");
                String dppId = doc.getString("dppId");
                ObjectNode dpp = mapper.createObjectNode().put("dppId", dppId);
                grouped.computeIfAbsent(productId, k -> new ArrayList<>()).add(dpp);
            }

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

    @PatchMapping("/dpps/{dppId}")
    public ResponseEntity<ObjectNode> updateDpp(
            @PathVariable String dppId,
            @RequestBody JsonNode updateData) {
        ObjectNode response = mapper.createObjectNode();

        try {
            Aggregation agg = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("dpps._id").is(dppId)),
                    Aggregation.unwind("dpps"),
                    Aggregation.match(Criteria.where("dpps._id").is(dppId)),
                    Aggregation.replaceRoot("dpps"));

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

            Query shellQuery = new Query(Criteria.where("dpps._id").is(dppId));
            org.bson.Document shellDoc = mongoTemplate.findOne(
                    shellQuery,
                    org.bson.Document.class,
                    "dpp-repo");

            if (shellDoc == null) {
                response.put("status", "failure");
                response.put("message", "Shell for DPP not found");
                return ResponseEntity.status(404).body(response);
            }

            String shellId = shellDoc.getString("_id");

            String newTimestamp = String.valueOf(Instant.now().toEpochMilli());
            String newDppId = productId + newTimestamp;

            MongoDppTemplate newDpp = new MongoDppTemplate();
            newDpp.setDppId(newDppId);
            newDpp.setProductId(productId);
            newDpp.setCreatedAt(newTimestamp);

            if (updateData.has("version")) {
                newDpp.setVersion(updateData.get("version").asText());
            } else {
                newDpp.setVersion(oldDpp.getString("version"));
            }

            if (updateData.has("submodels")) {
                List<MongoDppTemplate.Submodels> submodels = mapper.convertValue(
                        updateData.get("submodels"),
                        new TypeReference<List<MongoDppTemplate.Submodels>>() {
                        });
                newDpp.setSubmodels(submodels);
            } else {
                newDpp.setSubmodels(null);
            }

            Update deleteUpdate = new Update().pull("dpps", new org.bson.Document("_id", dppId));
            UpdateResult deleteResult = mongoTemplate.updateFirst(
                    shellQuery,
                    deleteUpdate,
                    "dpp-repo");

            if (deleteResult.getModifiedCount() == 0) {
                response.put("status", "warning");
                response.put("message", "No DPP removed (already gone or shell mismatch)");
            }

            Query insertQuery = new Query(Criteria.where("_id").is(shellId));
            Update insertUpdate = new Update().push("dpps", newDpp);
            UpdateResult insertResult = mongoTemplate.updateFirst(
                    insertQuery,
                    insertUpdate,
                    "dpp-repo");

            if (insertResult.getModifiedCount() == 0) {
                response.put("status", "failure");
                response.put("message", "No shell found to insert new DPP");
                return ResponseEntity.status(500).body(response);
            }

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

    // FR-BE-11: ReadElement - Abrufen eines Elements aus dem DPP per ID und elementPath
    @GetMapping("/dpps/{dppId}/elements/{elementPath}")
    public ResponseEntity<ObjectNode> readElement(
            @PathVariable String dppId,
            @PathVariable String elementPath) {
        ObjectNode response = mapper.createObjectNode();

        // FR-BE-13: Validierung der Eingabeparameter
        if (dppId == null || dppId.trim().isEmpty()) {
            logger.error("Invalid input: dppId is empty or null");
            response.put("status", "error");
            response.put("message", "Invalid parameter: dppId cannot be empty");
            return ResponseEntity.badRequest().body(response);
        }

        if (elementPath == null || elementPath.trim().isEmpty()) {
            logger.error("Invalid input: elementPath is empty or null");
            response.put("status", "error");
            response.put("message", "Invalid parameter: elementPath cannot be empty");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Retrieve the DPP document by dppId
            MongoDppTemplate dpp = APIUtilsDPP.getDppById(dppId, mongoTemplate);

            if (dpp == null) {
                logger.warn("DPP not found for dppId: {}", dppId);
                response.put("status", "failure");
                response.put("message", "DPP not found for the provided dppId");
                return ResponseEntity.status(404).body(response);
            }

            // Navigate through the DPP structure using the elementPath
            // elementPath format: "productId" or "submodels.0.name" etc.
            JsonNode dppJson = mapper.convertValue(dpp, JsonNode.class);
            JsonNode element = dppJson.at("/" + elementPath.replace(".", "/"));

            if (element == null || element.isMissingNode()) {
                logger.warn("Element not found at path: {} in dppId: {}", elementPath, dppId);
                response.put("status", "failure");
                response.put("message", "Element not found at the specified path: " + elementPath);
                return ResponseEntity.status(404).body(response);
            }

            response.put("status", "success");
            response.putPOJO("element", element);
            response.put("elementPath", elementPath);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving element from DPP: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Error retrieving element: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // FR-BE-12: UpdateElement - Updaten eines Elements aus dem DPP per ID und elementPath
    @PatchMapping("/dpps/{dppId}/elements/{elementPath}")
    public ResponseEntity<ObjectNode> updateElement(
            @PathVariable String dppId,
            @PathVariable String elementPath,
            @RequestBody JsonNode updateValue) {
        ObjectNode response = mapper.createObjectNode();

        // FR-BE-13: Validierung der Eingabeparameter
        if (dppId == null || dppId.trim().isEmpty()) {
            logger.error("Invalid input: dppId is empty or null");
            response.put("status", "error");
            response.put("message", "Invalid parameter: dppId cannot be empty");
            return ResponseEntity.badRequest().body(response);
        }

        if (elementPath == null || elementPath.trim().isEmpty()) {
            logger.error("Invalid input: elementPath is empty or null");
            response.put("status", "error");
            response.put("message", "Invalid parameter: elementPath cannot be empty");
            return ResponseEntity.badRequest().body(response);
        }

        if (updateValue == null) {
            logger.error("Invalid input: updateValue is null");
            response.put("status", "error");
            response.put("message", "Invalid parameter: updateValue cannot be empty");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Retrieve the current DPP document
            MongoDppTemplate dpp = APIUtilsDPP.getDppById(dppId, mongoTemplate);

            if (dpp == null) {
                logger.warn("DPP not found for dppId: {}", dppId);
                response.put("status", "failure");
                response.put("message", "DPP not found for the provided dppId");
                return ResponseEntity.status(404).body(response);
            }

            // Convert the path notation to MongoDB update notation
            // e.g., "productId" -> "dpps.0.productId"
            // e.g., "submodels.0.name" -> "dpps.0.submodels.0.name"
            String mongoPath = "dpps.0." + elementPath;

            // Build the update query using MongoDB Update
            Query query = new Query(Criteria.where("dpps._id").is(dppId));
            Update update = new Update().set(mongoPath, updateValue);

            UpdateResult result = mongoTemplate.updateFirst(query, update, "dpp-repo");

            if (result.getMatchedCount() == 0) {
                logger.warn("DPP not found for update with dppId: {}", dppId);
                response.put("status", "failure");
                response.put("message", "DPP not found for the provided dppId");
                return ResponseEntity.status(404).body(response);
            }

            if (result.getModifiedCount() == 0) {
                logger.warn("Element not updated at path: {} in dppId: {}", elementPath, dppId);
                response.put("status", "warning");
                response.put("message", "No changes made - element may not exist at the specified path");
                return ResponseEntity.ok(response);
            }

            response.put("status", "success");
            response.put("message", "Element updated successfully");
            response.put("elementPath", elementPath);
            response.put("modifiedCount", result.getModifiedCount());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating element in DPP: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Error updating element: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}