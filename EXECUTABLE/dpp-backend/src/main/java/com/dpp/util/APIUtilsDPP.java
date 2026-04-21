package com.dpp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.client.RestClient;

import com.dpp.MongoDppTemplate;
import com.dpp.MongoDppTemplate.Submodels;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.slf4j.Logger;

public class APIUtilsDPP {

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

    public static ObjectNode collectSubmodelData(ObjectMapper mapper, MongoDppTemplate dpp, RestClient restClient,
            Logger logger) {
        ObjectNode collctedSubmodels = mapper.createObjectNode();

        for (Submodels submodel : dpp.getSubmodels()) {

            try {
                String externalApiBase = System.getenv("EXTERNAL_AAS_API_URL");
if (externalApiBase == null || externalApiBase.isEmpty()) {
    externalApiBase = "http://localhost:8081";
}

                String externalUrl = externalApiBase
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

    /**
     * Helper to extract submodels based on a whitelist and keyword search anywhere
     * in the path.
     */
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
