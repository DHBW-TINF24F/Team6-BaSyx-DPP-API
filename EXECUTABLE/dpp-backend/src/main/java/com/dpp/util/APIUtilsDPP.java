package com.dpp.util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import com.dpp.MongoDppTemplate;
import com.dpp.MongoDppTemplate.Submodels;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class APIUtilsDPP {

    private static final Map<Integer, String> ERROR_MAP;

    public static MongoDppTemplate getDppById(String dppId, MongoTemplate mongoTemplate) {

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("dpps._id").is(dppId)),
                Aggregation.unwind("dpps"),
                Aggregation.match(Criteria.where("dpps._id").is(dppId)),
                Aggregation.replaceRoot("dpps"));

        List<org.bson.Document> results = mongoTemplate.aggregate(
                aggregation, "dpp-repo", org.bson.Document.class).getMappedResults();

        if (results.isEmpty()) {
            return null;
        }

        return mongoTemplate.getConverter().read(
                MongoDppTemplate.class,
                results.get(0));

    }

    public static ObjectNode collectAASNameAndDescription(RestClient restClient, Logger logger, ObjectMapper mapper, MongoDppTemplate dpp) {
            ObjectNode response = mapper.createObjectNode();
           try {
            String externalApiBase = System.getenv("EXTERNAL_AAS_API_URL");
            if (externalApiBase == null || externalApiBase.isEmpty()) {
                externalApiBase = "http://localhost:8081";
            }

            String externalUrl = externalApiBase + "/shells";

            // Using RestClient (Blocking/Synchronous)
            JsonNode externalPayload = restClient.get()
                    .uri(externalUrl)
                    .retrieve()
                    .body(JsonNode.class);


            if (externalPayload == null || !externalPayload.has("result")) {
            logger.info("External API call failed for /shells");
                return null;
            }
            logger.info("External API call successful for /shells");

            for(JsonNode entry : externalPayload.get("result")) {
                String entryID = Base64DPP.ensureEncoding(entry.get("id").asText());
                if (entryID.equals(Base64DPP.ensureEncoding(dpp.getProductId()))) {
                    response.putPOJO("name", entry.get("idShort"));
                    response.putPOJO("description", entry.get("description"));
                    return response;
                }
            }
            logger.info("Did not find aasIdentifyer {}",dpp.getProductId());


        } catch (Exception apiEx) {
            logger.warn("External API call to :8081 failed: {}", apiEx.getMessage());
        }
        return null;

    }

    public static MongoDppTemplate getDppByAggregatoin(Aggregation aggregation, MongoTemplate mongoTemplate) {

        List<org.bson.Document> results = mongoTemplate.aggregate(
                aggregation, "dpp-repo", org.bson.Document.class).getMappedResults();

        if (results.isEmpty()) {
            return null;
        }

        return mongoTemplate.getConverter().read(
                MongoDppTemplate.class,
                results.get(0));

    }


    public static ObjectNode collectAdministration(ObjectMapper mapper, MongoDppTemplate dpp, RestClient restClient,
            Logger logger) {

        try {
            String externalApiBase = System.getenv("EXTERNAL_AAS_API_URL");
            if (externalApiBase == null || externalApiBase.isEmpty()) {
                externalApiBase = "http://localhost:8081";
            }

            String externalUrl = externalApiBase + "/shells/"
                    + Base64DPP.ensureEncoding(dpp.getProductId());

            // Using RestClient (Blocking/Synchronous)
            JsonNode externalPayload = restClient.get()
                    .uri(externalUrl)
                    .retrieve()
                    .body(JsonNode.class);

            logger.info("External API call successful for /shells/{}", dpp.getProductId());

            if (externalPayload != null) {
                ObjectNode collectedAdministration = mapper.createObjectNode();
                collectedAdministration.putPOJO("administration", externalPayload.get("administration"));
                return collectedAdministration;
            }

        } catch (Exception apiEx) {
            logger.warn("External API call to :8081 failed: {}", apiEx.getMessage());
        }

        return null;
    }


    public static ObjectNode collectAssetInformation(ObjectMapper mapper, MongoDppTemplate dpp, RestClient restClient,
            Logger logger) {

        try {
            String externalApiBase = System.getenv("EXTERNAL_AAS_API_URL");
            if (externalApiBase == null || externalApiBase.isEmpty()) {
                externalApiBase = "http://localhost:8081";
            }

            String externalUrl = externalApiBase + "/shells/"
                    + Base64DPP.ensureEncoding(dpp.getProductId());

            // Using RestClient (Blocking/Synchronous)
            JsonNode externalPayload = restClient.get()
                    .uri(externalUrl)
                    .retrieve()
                    .body(JsonNode.class);

            logger.info("External API call successful for /shells/{}", dpp.getProductId());

            if (externalPayload != null) {
                ObjectNode collectedAssetInformation = mapper.createObjectNode();
                collectedAssetInformation.putPOJO("assetInformation", externalPayload.get("assetInformation"));
                return collectedAssetInformation;
            }

        } catch (Exception apiEx) {
            logger.warn("External API call to :8081 failed: {}", apiEx.getMessage());
        }

        return null;
    }

    public static ObjectNode collectSubmodelData(ObjectMapper mapper, MongoDppTemplate dpp, RestClient restClient,
            Logger logger) {
        ObjectNode collctedSubmodels = mapper.createObjectNode();

        for (Submodels submodel : dpp.getSubmodels()) {

            try {
                String externalApiBase = System.getenv("EXTERNAL_AAS_API_URL");
                if (externalApiBase == null || externalApiBase.isEmpty()) {
                    externalApiBase = "http://localhost:8081";
                }

                String externalUrl = externalApiBase + "/submodels/"
                        + Base64DPP.encodeIdentifier(submodel.getReference()) + "/submodel-elements";

                // Using RestClient (Blocking/Synchronous)
                JsonNode externalPayload = restClient.get()
                        .uri(externalUrl)
                        .retrieve()
                        .body(JsonNode.class);

                logger.info("External API call successful for submodel: {}", submodel);

                if (externalPayload != null && externalPayload.has("result")) {
                    collctedSubmodels.putPOJO(submodel.getName(), externalPayload.get("result"));
                }

            } catch (Exception apiEx) {
                logger.warn("External API call to :8081 failed: {}", apiEx.getMessage());
            }
        }

        return collctedSubmodels;
    }

    static {
        Map<Integer, String> map = new HashMap<>();

        // Mapping basierend auf prEN 18222:2025, Tabelle 16
        map.put(200, "Success");
        map.put(201, "SuccessCreated");
        map.put(202, "SuccessAccepted");
        map.put(204, "SuccessNoContent");
        map.put(400, "ClientErrorBadRequest");
        map.put(401, "ClientNotAuthorized");
        map.put(403, "ClientForbidden");
        map.put(404, "ClientErrorResourceNotFound");
        map.put(405, "ClientMethodNotAllowed");
        map.put(409, "ClientResourceConflict");
        map.put(500, "ServerInternalError");
        map.put(502, "ServerErrorBadGateway");

        ERROR_MAP = Collections.unmodifiableMap(map);
    }

    private static String getStatusCode(int errorCode) {
        return ERROR_MAP.getOrDefault(errorCode, "UnknownError");
    }

    public static ResponseEntity<ObjectNode> create_generic_response(int code, String text, String messageType,
            ObjectMapper mapper) {
        ObjectNode response = mapper.createObjectNode();
        ObjectNode inner = mapper.createObjectNode();
        ArrayNode arr = response.putArray("message");

        inner.put("code", code);
        inner.put("messageType", messageType);
        inner.put("correlationId", getStatusCode(code));
        inner.put("text", text);
        inner.put("timeStamp", Instant.now().toString());

        arr.add(inner);

        return ResponseEntity.status(code).body(response);
    }

    public static List<MongoDppTemplate.Submodels> extractAndFilterSubmodels(JsonNode resultsArray) {
        List<MongoDppTemplate.Submodels> list = new ArrayList<>();

        // Target submodels and their standardized names
        Map<String, String> submodelMap = new HashMap<>();
        submodelMap.put("nameplate", "NamePlate");
        submodelMap.put("circularity", "Circularity");
        submodelMap.put("carbonfootprint", "CarbonFootPrint");
        submodelMap.put("handoverdocumentation", "HandoverDocumentation");
        submodelMap.put("technicaldata", "TechnicalData");
        submodelMap.put("productcondition", "ProductCondition");
        submodelMap.put("materialcomposition", "MaterialComposition");

        for (JsonNode entry : resultsArray) {
            JsonNode keys = entry.path("keys");
            if (keys.isArray() && !keys.isEmpty()) {
                String fullPath = keys.get(0).path("value").asText();

                // Normalization: remove underscores, dots, hyphens, and slashes
                String normalizedPath = fullPath.toLowerCase()
                        .replace("_", "")
                        .replace("-", "")
                        .replace(".", "")
                        .replace("/", "");

                for (Map.Entry<String, String> target : submodelMap.entrySet()) {
                    if (normalizedPath.contains(target.getKey())) {
                        list.add(new MongoDppTemplate.Submodels(fullPath, target.getValue(), "1.0"));
                        break;
                    }
                }
            }
        }
        return list;
    }

}
