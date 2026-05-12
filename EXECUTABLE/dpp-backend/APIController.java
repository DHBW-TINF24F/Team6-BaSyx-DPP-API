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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import com.dpp.MongoDppTemplate;
import com.dpp.MongoDppTemplate.Submodels;
import com.dpp.api.DppResponse.ApiResponse;
import com.dpp.api.DppResponse.MessageTypeEnum;
import com.dpp.api.DppResponse.Result;
import com.dpp.api.DppResponse.StatusCode;
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

    private final ObjectMapper  mapper;
    private final RestClient    restClient = RestClient.create();
    private final MongoTemplate mongoTemplate;

    public APIController(ObjectMapper mapper, MongoTemplate mongoTemplate) {
        this.mapper        = mapper;
        this.mongoTemplate = mongoTemplate;
    }

    // =========================================================================
    //  GET /health  – not part of DIN, keep as-is
    // =========================================================================
    @GetMapping("/health")
    public ResponseEntity<ObjectNode> health() {
        ObjectNode node = mapper.createObjectNode();
        node.put("status", "UP");
        return ResponseEntity.ok(node);
    }

    // =========================================================================
    //  POST /dpps  –  CreateDPP  (DIN §4.6 / Tabelle 5)
    //
    //  Success  → HTTP 201  SuccessCreated  + { statusCode, payload: { dppId } }
    //  Failure  → HTTP 4xx/5xx + { statusCode, result: { message: [...] } }
    // =========================================================================
    @PostMapping("/dpps")
    public ResponseEntity<?> createDpp(@RequestBody JsonNode dpp) {

        // --- Validate input --------------------------------------------------
        if (!ValidateDPP.validateJsonTillFirstEntry(dpp)) {
            logger.error("Validation failed for incoming DPP");
            ApiResponse<?> resp = ApiResponse.badRequest("Invalid DPP structure.");
            return ResponseEntity.badRequest().body(resp);
        }

        try {
            // 1. Shell-ID as document _id
            String shellId  = dpp.get("shell").get("id").asText();
            JsonNode dppEntry = dpp.get("shell").get("dpps").get(0);

            // 2. Build dppId
            String timeStamp = String.valueOf(Instant.now().toEpochMilli());
            String dppId     = Base64DPP.ensureEncoding(dppEntry.get("productId").asText() + timeStamp);
            String aasId     = Base64DPP.ensureEncoding(dppEntry.get("productId").asText());

            // 3. Fetch submodels from external AAS registry (best-effort)
            List<MongoDppTemplate.Submodels> filteredSubmodels = new ArrayList<>();
            try {
                String externalApiBase = System.getenv("EXTERNAL_AAS_API_URL");
                if (externalApiBase == null || externalApiBase.isEmpty()) {
                    externalApiBase = "http://localhost:8081";
                }
                String externalUrl = externalApiBase + "/shells/" + aasId + "/submodel-refs";

                JsonNode externalPayload = restClient.get()
                        .uri(externalUrl)
                        .retrieve()
                        .body(JsonNode.class);

                logger.info("External AAS call successful for: {}", aasId);

                if (externalPayload != null && externalPayload.has("result")) {
                    filteredSubmodels = APIUtilsDPP.extractAndFilterSubmodels(
                            externalPayload.get("result"));
                }
            } catch (Exception apiEx) {
                logger.warn("External AAS call failed: {}", apiEx.getMessage());
            }

            // 4. Map to POJO and enrich
            MongoDppTemplate dppContent = mapper.treeToValue(dppEntry, MongoDppTemplate.class);
            dppContent.setCreatedAt(timeStamp);
            dppContent.setDppId(dppId);
            dppContent.setSubmodels(filteredSubmodels);
            dppContent.setProductId(Base64DPP.ensureEncoding(dppContent.getProductId()));

            // 5. Upsert into MongoDB
            Query  query  = new Query(Criteria.where("_id").is(shellId));
            Update update = new Update().push("dpps", dppContent);
            mongoTemplate.upsert(query, update, "dpp-repo");

            logger.info("Shell {} upserted, dppId={}", shellId, dppId);

            // 6. DIN-compliant success response  ─  SuccessCreated + dppId as payload
            ObjectNode payloadNode = mapper.createObjectNode();
            payloadNode.put("dppId", dppId);

            ApiResponse<ObjectNode> resp = ApiResponse.successCreated(payloadNode);
            return ResponseEntity.status(201).body(resp);

        } catch (Exception e) {
            logger.error("Error in createDpp", e);
            ApiResponse<?> resp = ApiResponse.internalError(e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    // =========================================================================
    //  GET /dpps/{dppId}  –  ReadDPPById  (DIN §4.2 / Tabelle 1)
    //
    //  Success  → HTTP 200  Success  + { statusCode, payload: <DPP> }
    //  Not Found → HTTP 404  ClientErrorResourceNotFound  + result
    // =========================================================================
    @GetMapping("/dpps/{dppId}")
    public ResponseEntity<?> readDppById(@PathVariable String dppId) {

        try {
            Aggregation agg = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("dpps._id").is(dppId)),
                    Aggregation.unwind("dpps"),
                    Aggregation.match(Criteria.where("dpps._id").is(dppId)),
                    Aggregation.replaceRoot("dpps"));

            List<org.bson.Document> results = mongoTemplate
                    .aggregate(agg, "dpp-repo", org.bson.Document.class)
                    .getMappedResults();

            if (results.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.notFound("DPP with id '" + dppId + "' not found."));
            }

            MongoDppTemplate dppObj = mongoTemplate.getConverter()
                    .read(MongoDppTemplate.class, results.get(0));

            ObjectNode submodelValues = APIUtilsDPP.collectSubmodelData(
                    mapper, dppObj, restClient, logger);

            // Build payload node: dpp + submodels_values
            ObjectNode payloadNode = mapper.createObjectNode();
            payloadNode.putPOJO("dpp", dppObj);
            payloadNode.set("submodels_values", submodelValues);

            ApiResponse<ObjectNode> resp = ApiResponse.success(payloadNode);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            logger.error("Error in readDppById: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.internalError(e.getMessage()));
        }
    }

    // =========================================================================
    //  GET /dppsByProductId/{productId}  –  ReadDPPByProductId  (DIN §4.3)
    //
    //  Returns the *latest* DPP version for the given product ID.
    // =========================================================================
    @GetMapping("/dppsByProductId/{productId}")
    public ResponseEntity<?> readDppByProductId(@PathVariable String productId) {

        try {
            Aggregation agg = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("dpps.productId").is(productId)),
                    Aggregation.unwind("dpps"),
                    Aggregation.match(Criteria.where("dpps.productId").is(productId)),
                    Aggregation.replaceRoot("dpps"));

            List<org.bson.Document> results = mongoTemplate
                    .aggregate(agg, "dpp-repo", org.bson.Document.class)
                    .getMappedResults();

            if (results.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.notFound(
                                "No DPP found for productId '" + productId + "'."));
            }

            // Last entry = latest version
            MongoDppTemplate dppObj = mongoTemplate.getConverter()
                    .read(MongoDppTemplate.class, results.get(results.size() - 1));

            ObjectNode submodelValues = APIUtilsDPP.collectSubmodelData(
                    mapper, dppObj, restClient, logger);

            ObjectNode payloadNode = mapper.createObjectNode();
            payloadNode.putPOJO("dpp", dppObj);
            payloadNode.set("submodels_values", submodelValues);

            return ResponseEntity.ok(ApiResponse.success(payloadNode));

        } catch (Exception e) {
            logger.error("Error in readDppByProductId: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.internalError(e.getMessage()));
        }
    }

    // =========================================================================
    //  GET /dppsByProductIdAndDate/{productId}?timeStamp=…
    //    –  ReadDPPVersionByProductIdAndDate  (DIN §4.4 / Tabelle 3)
    // =========================================================================
    @GetMapping("/dppsByProductIdAndDate/{productId}")
    public ResponseEntity<?> readDppByProductIdAndDate(
            @PathVariable String productId,
            @RequestParam  String timeStamp) {

        try {
            Aggregation agg = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("dpps.productId").is(productId)
                            .and("dpps.createdAt").is(timeStamp)),
                    Aggregation.unwind("dpps"),
                    Aggregation.match(Criteria.where("dpps.productId").is(productId)
                            .and("dpps.createdAt").is(timeStamp)),
                    Aggregation.replaceRoot("dpps"));

            List<org.bson.Document> results = mongoTemplate
                    .aggregate(agg, "dpp-repo", org.bson.Document.class)
                    .getMappedResults();

            if (results.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.notFound(
                                "No DPP found for productId '" + productId
                                + "' at timestamp '" + timeStamp + "'."));
            }

            MongoDppTemplate dppObj = mongoTemplate.getConverter()
                    .read(MongoDppTemplate.class, results.get(0));

            ObjectNode payloadNode = mapper.createObjectNode();
            payloadNode.putPOJO("dpp", dppObj);

            return ResponseEntity.ok(ApiResponse.success(payloadNode));

        } catch (Exception e) {
            logger.error("Error in readDppByProductIdAndDate: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.internalError(e.getMessage()));
        }
    }

    // =========================================================================
    //  POST /dppIdsByProductIds  –  ReadDPPIdsByProductIds  (DIN §4.5 / Tabelle 4)
    //
    //  Input:  List<String> productIds
    //  Output: { statusCode, payload: { "<productId>": [{ dppId }], … } }
    // =========================================================================
    @PostMapping("/dppIdsByProductIds")
    public ResponseEntity<?> getDppIdsByProductIds(@RequestBody List<String> productIds) {

        try {
            Aggregation agg = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("dpps.productId").in(productIds)),
                    Aggregation.unwind("dpps"),
                    Aggregation.match(Criteria.where("dpps.productId").in(productIds)),
                    Aggregation.project()
                            .and("dpps.productId").as("productId")
                            .and("dpps._id").as("dppId"));

            List<org.bson.Document> results = mongoTemplate
                    .aggregate(agg, "dpp-repo", org.bson.Document.class)
                    .getMappedResults();

            Map<String, List<ObjectNode>> grouped = new HashMap<>();
            for (org.bson.Document doc : results) {
                String pid  = doc.getString("productId");
                String did  = doc.getString("dppId");
                ObjectNode entry = mapper.createObjectNode().put("dppId", did);
                grouped.computeIfAbsent(pid, k -> new ArrayList<>()).add(entry);
            }

            ObjectNode payloadNode = mapper.convertValue(grouped, ObjectNode.class);
            return ResponseEntity.ok(ApiResponse.success(payloadNode));

        } catch (Exception e) {
            logger.error("Error in getDppIdsByProductIds: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.internalError(e.getMessage()));
        }
    }

    // =========================================================================
    //  PATCH /dpps/{dppId}  –  UpdateDPPById  (DIN §4.7 / Tabelle 6)
    //
    //  DIN: partial update, returns the updated DPP.
    //  The previous implementation replaced the whole DPP entry (PUT semantics).
    //  This implementation merges only the supplied fields into the existing DPP.
    // =========================================================================
    @PatchMapping("/dpps/{dppId}")
    public ResponseEntity<?> updateDpp(
            @PathVariable String dppId,
            @RequestBody  JsonNode updateData) {

        try {
            // 1. Locate the existing DPP entry
            MongoDppTemplate existing = APIUtilsDPP.getDppById(dppId, mongoTemplate);
            if (existing == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.notFound("DPP '" + dppId + "' not found."));
            }

            // 2. Apply partial update  (only fields present in the request body)
            if (updateData.has("version")) {
                existing.setVersion(updateData.get("version").asText());
            }
            if (updateData.has("submodels")) {
                List<MongoDppTemplate.Submodels> submodels = mapper.convertValue(
                        updateData.get("submodels"),
                        new TypeReference<List<MongoDppTemplate.Submodels>>() {});
                existing.setSubmodels(submodels);
            }
            // Additional fields can be merged here as the data model grows.

            // 3. Persist – update the matching array element in-place
            Query  query  = new Query(Criteria.where("dpps._id").is(dppId));
            Update update = new Update().set("dpps.$", existing);
            UpdateResult result = mongoTemplate.updateFirst(query, update, "dpp-repo");

            if (result.getModifiedCount() == 0) {
                // DIN: if update of any part fails, no changes should be adopted
                return ResponseEntity.status(404)
                        .body(ApiResponse.notFound(
                                "DPP '" + dppId + "' could not be updated (not found in shell)."));
            }

            // 4. Return the updated DPP as payload  (DIN Tabelle 6: Nutzlast → DPP)
            ObjectNode payloadNode = mapper.createObjectNode();
            payloadNode.putPOJO("dpp", existing);

            return ResponseEntity.ok(ApiResponse.success(payloadNode));

        } catch (Exception e) {
            logger.error("Error in updateDpp: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.internalError(e.getMessage()));
        }
    }

    // =========================================================================
    //  DELETE /dpps/{dppId}  –  DeleteDPPById  (DIN §4.8 / Tabelle 7)
    //
    //  Success → HTTP 200  SuccessNoContent  (no payload)
    // =========================================================================
    @DeleteMapping("/dpps/{dppId}")
    public ResponseEntity<?> deleteDpp(@PathVariable String dppId) {

        try {
            Query  query  = new Query(Criteria.where("dpps._id").is(dppId));
            Update update = new Update().pull("dpps", new org.bson.Document("_id", dppId));
            UpdateResult result = mongoTemplate.updateFirst(query, update, "dpp-repo");

            if (result.getModifiedCount() == 0) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.notFound(
                                "No DPP entry found for id '" + dppId + "'."));
            }

            // DIN: DeleteDPPById has no payload in the success case
            return ResponseEntity.ok(ApiResponse.successNoContent());

        } catch (Exception e) {
            logger.error("Error in deleteDpp: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.internalError(e.getMessage()));
        }
    }

    // =========================================================================
    //  GET /dpps/{dppId}/collections/{elementId}
    //    –  ReadDataElementCollection  (DIN §6.2 / Tabelle 9)
    // =========================================================================
    @GetMapping("/dpps/{dppId}/collections/{elementId}")
    public ResponseEntity<?> readElementCollection(
            @PathVariable String dppId,
            @PathVariable String elementId) {

        elementId = Base64DPP.ensureEncoding(elementId);

        try {
            MongoDppTemplate dppObj = APIUtilsDPP.getDppByAggregatoin(
                    Aggregation.newAggregation(
                            Aggregation.match(Criteria.where("dpps._id").is(dppId)),
                            Aggregation.unwind("dpps"),
                            Aggregation.match(Criteria.where("dpps._id").is(dppId)),
                            Aggregation.replaceRoot("dpps")),
                    mongoTemplate);

            if (dppObj == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.notFound("DPP '" + dppId + "' not found."));
            }

            for (Submodels submodel : dppObj.getSubmodels()) {
                String submodelBase64 = Base64DPP.ensureEncoding(submodel.getReference());

                if (submodelBase64.equals(elementId)) {
                    try {
                        String externalApiBase = System.getenv("EXTERNAL_AAS_API_URL");
                        if (externalApiBase == null || externalApiBase.isEmpty()) {
                            externalApiBase = "http://localhost:8081";
                        }
                        String externalUrl = externalApiBase
                                + "/shells/" + submodelBase64 + "/submodel-refs";

                        JsonNode externalPayload = restClient.get()
                                .uri(externalUrl)
                                .retrieve()
                                .body(JsonNode.class);

                        logger.info("External AAS call successful for submodel: {}", submodel);

                        if (externalPayload != null && externalPayload.has("result")) {
                            ObjectNode payloadNode = mapper.createObjectNode();
                            payloadNode.set("dataElementCollection",
                                    externalPayload.get("result"));
                            return ResponseEntity.ok(ApiResponse.success(payloadNode));
                        }

                    } catch (Exception apiEx) {
                        logger.warn("External AAS call failed: {}", apiEx.getMessage());
                        return ResponseEntity.status(502)
                                .body(ApiResponse.<ObjectNode>internalError(
                                        "External AAS registry unreachable: "
                                        + apiEx.getMessage()));
                    }
                }
            }

            // elementId not found in any submodel
            return ResponseEntity.status(404)
                    .body(ApiResponse.notFound(
                            "Element collection '" + elementId
                            + "' not found in DPP '" + dppId + "'."));

        } catch (Exception e) {
            logger.error("Error in readElementCollection: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.internalError(e.getMessage()));
        }
    }

    // =========================================================================
    //  PATCH /dpps/{dppId}/collections/{elementId}
    //    –  UpdateDataElementCollection  (DIN §6.4 / Tabelle 11)
    //
    //  Success → HTTP 200  Success  + { statusCode, payload: <updated collection> }
    //            (payload is optional per DIN – we include it when available)
    // =========================================================================
    @PatchMapping("/dpps/{dppId}/collections/{elementId}")
    public ResponseEntity<?> updateDataElementCollection(
            @PathVariable String dppId,
            @PathVariable String elementId,
            @RequestBody  JsonNode body) {

        elementId = Base64DPP.ensureEncoding(elementId);

        try {
            MongoDppTemplate dppObj = APIUtilsDPP.getDppById(dppId, mongoTemplate);

            if (dppObj == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.notFound("DPP '" + dppId + "' not found."));
            }

            for (Submodels submodel : dppObj.getSubmodels()) {
                String submodelBase64 = Base64DPP.ensureEncoding(submodel.getReference());

                if (submodelBase64.equals(elementId)) {
                    try {
                        String externalApiBase = System.getenv("EXTERNAL_AAS_API_URL");
                        if (externalApiBase == null || externalApiBase.isEmpty()) {
                            externalApiBase = "http://localhost:8081";
                        }
                        String externalUrl = externalApiBase
                                + "/shells/" + submodelBase64 + "/submodel-refs";

                        JsonNode externalPayload = restClient.patch()
                                .uri(externalUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(body)
                                .retrieve()
                                .body(JsonNode.class);

                        logger.info("External PATCH successful for submodel: {}", submodel);

                        if (externalPayload != null && externalPayload.has("result")) {
                            // Return updated collection as payload
                            ObjectNode payloadNode = mapper.createObjectNode();
                            payloadNode.set("dataElementCollection",
                                    externalPayload.get("result"));
                            return ResponseEntity.ok(ApiResponse.success(payloadNode));
                        }

                        // External call succeeded but returned no result body →
                        // DIN allows optional payload for UpdateDataElementCollection
                        return ResponseEntity.ok(ApiResponse.<ObjectNode>successNoContent());

                    } catch (Exception apiEx) {
                        logger.warn("External PATCH call failed: {}", apiEx.getMessage());
                        return ResponseEntity.status(502)
                                .body(ApiResponse.<ObjectNode>internalError(
                                        "External AAS registry unreachable: "
                                        + apiEx.getMessage()));
                    }
                }
            }

            // elementId not matched
            return ResponseEntity.status(404)
                    .body(ApiResponse.notFound(
                            "Element collection '" + elementId
                            + "' not found in DPP '" + dppId + "'."));

        } catch (Exception e) {
            logger.error("Error in updateDataElementCollection: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.internalError(e.getMessage()));
        }
    }

    // =========================================================================
    //  PUT /dpps/{dppId}  –  kept for backwards-compatibility only.
    //  The DIN standard uses PATCH for updates; new consumers should use
    //  PATCH /dpps/{dppId} instead.
    //  This endpoint performs a full replacement and creates a new version
    //  (old behaviour preserved intentionally).
    // =========================================================================
    @PutMapping("/dpps/{dppId}")
    public ResponseEntity<?> replaceDpp(
            @PathVariable String dppId,
            @RequestBody  JsonNode updateData) {

        try {
            Aggregation agg = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("dpps._id").is(dppId)),
                    Aggregation.unwind("dpps"),
                    Aggregation.match(Criteria.where("dpps._id").is(dppId)),
                    Aggregation.replaceRoot("dpps"));

            List<org.bson.Document> oldDppDocs = mongoTemplate
                    .aggregate(agg, "dpp-repo", org.bson.Document.class)
                    .getMappedResults();

            if (oldDppDocs.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.notFound("DPP '" + dppId + "' not found."));
            }

            org.bson.Document oldDpp   = oldDppDocs.get(0);
            String            productId = oldDpp.getString("productId");

            Query shellQuery = new Query(Criteria.where("dpps._id").is(dppId));
            org.bson.Document shellDoc = mongoTemplate.findOne(
                    shellQuery, org.bson.Document.class, "dpp-repo");

            if (shellDoc == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.notFound(
                                "Shell for DPP '" + dppId + "' not found."));
            }

            String shellId       = shellDoc.getString("_id");
            String newTimestamp  = String.valueOf(Instant.now().toEpochMilli());
            String newDppId      = Base64DPP.ensureEncoding(productId + newTimestamp);

            MongoDppTemplate newDpp = new MongoDppTemplate();
            newDpp.setDppId(newDppId);
            newDpp.setProductId(productId);
            newDpp.setCreatedAt(newTimestamp);
            newDpp.setVersion(updateData.has("version")
                    ? updateData.get("version").asText()
                    : oldDpp.getString("version"));

            if (updateData.has("submodels")) {
                newDpp.setSubmodels(mapper.convertValue(
                        updateData.get("submodels"),
                        new TypeReference<List<MongoDppTemplate.Submodels>>() {}));
            } else {
                newDpp.setSubmodels(null);
            }

            // Remove old, insert new
            Update deleteUpdate = new Update()
                    .pull("dpps", new org.bson.Document("_id", dppId));
            mongoTemplate.updateFirst(shellQuery, deleteUpdate, "dpp-repo");

            Update insertUpdate = new Update().push("dpps", newDpp);
            UpdateResult insertResult = mongoTemplate.updateFirst(
                    new Query(Criteria.where("_id").is(shellId)),
                    insertUpdate, "dpp-repo");

            if (insertResult.getModifiedCount() == 0) {
                return ResponseEntity.status(500)
                        .body(ApiResponse.internalError(
                                "Could not insert replacement DPP into shell '" + shellId + "'."));
            }

            ObjectNode payloadNode = mapper.createObjectNode();
            payloadNode.putPOJO("dpp", newDpp);

            return ResponseEntity.ok(ApiResponse.success(payloadNode));

        } catch (Exception e) {
            logger.error("Error in replaceDpp: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.internalError(e.getMessage()));
        }
    }
}
