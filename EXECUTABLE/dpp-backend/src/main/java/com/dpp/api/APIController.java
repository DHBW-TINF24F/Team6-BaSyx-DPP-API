package com.dpp.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dpp.MongoDppInit;
import com.dpp.util.ValidateDPP;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
            JsonNode firstDppEntry = dpp.get("shell").get("dpps").get(0);
            
            // Map the JsonNode to our MongoDppInit POJO
            MongoDppInit dppContent = mapper.treeToValue(firstDppEntry, MongoDppInit.class);

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
            response.put("message", "DPP content appended to shell ID: " + shellId);
            return ResponseEntity.status(201).body(response);

        } catch (Exception e) {
            logger.error("Error during MongoDB operation", e);
            response.put("error", "Internal Server Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}