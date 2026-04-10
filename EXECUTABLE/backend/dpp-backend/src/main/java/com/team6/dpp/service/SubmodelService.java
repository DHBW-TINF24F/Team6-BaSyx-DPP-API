package com.team6.dpp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SubmodelService {

    private static final Logger logger = LoggerFactory.getLogger(SubmodelService.class);

    private final RestClient restClient;
    private final WebClient webClient;
    private final ObjectMapper mapper;

    public SubmodelService(RestClient restClient, WebClient webClient, ObjectMapper mapper) {
        this.restClient = restClient;
        this.webClient = webClient;
        this.mapper = mapper;
    }

    /**
     * Maps submodel URL to a standardized name
     */
    public String mapSubmodelName(String submodelUrl) {
        if (submodelUrl == null || submodelUrl.isBlank()) {
            return null;
        }
        String lower = submodelUrl.toLowerCase();
        if (lower.contains("nameplate")) {
            return "Digital Nameplate";
        } else if (lower.contains("handover")) {
            return "Handover Documentation";
        } else if (lower.contains("carbon")) {
            return "CarbonFootprint";
        } else if (lower.contains("technical")) {
            return "TechnicalData";
        } else if (lower.contains("condition")) {
            return "Condition";
        } else if (lower.contains("material")) {
            return "Material Composition";
        } else if (lower.contains("circular")) {
            return "Circularity";
        }
        return null;
    }

    /**
     * Fetches submodel payload data
     */
    public JsonNode fetchSubmodelPayload(String submodelUrl) {
        try {
            // WARNING: this is hardcoded for now
            String valueUrl = "https://dpp40.harting.com:8081/submodels/" + com.team6.dpp.util.DppUtils.encode(submodelUrl);
            logger.info("Fetching submodel payload from: {}", valueUrl);

            try {
                return restClient.get()
                        .uri(valueUrl)
                        .retrieve()
                        .body(JsonNode.class);
            } catch (Exception e) {
                logger.warn("RestClient failed, trying WebClient for: {}", valueUrl);
                // Fallback: versuche mit WebClient, aber ignoriere Non-JSON Responses
                try {
                    return webClient.get()
                            .uri(valueUrl)
                            .retrieve()
                            .bodyToMono(JsonNode.class)
                            .onErrorReturn(null)
                            .block();
                } catch (Exception webClientEx) {
                    logger.warn("WebClient also failed for {}: {}", valueUrl, webClientEx.getMessage());
                    return null;
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch submodel payload for {}: {}", submodelUrl, e.getMessage());
            return null;
        }
    }

    /**
     * Fetches submodel metadata
     */
    public JsonNode fetchSubmodelMetadata(String submodelUrl) {
        try {
            String metadataUrl = submodelUrl.endsWith("/") ? submodelUrl + "$metadata" : submodelUrl + "/$metadata";
            logger.info("Fetching submodel metadata from: {}", metadataUrl);

            try {
                JsonNode metadata = restClient.get()
                        .uri(metadataUrl)
                        .retrieve()
                        .body(JsonNode.class);

                if (metadata != null && metadata.isObject()) {
                    ObjectNode meta = (ObjectNode) metadata;
                    if (meta.has("result")) {
                        meta = (ObjectNode) meta.get("result");
                    }
                    return meta;
                }
                return null;
            } catch (Exception e) {
                logger.warn("RestClient failed, trying WebClient for metadata: {}", metadataUrl);
                // Fallback: versuche mit WebClient
                try {
                    JsonNode metadata = webClient.get()
                            .uri(metadataUrl)
                            .retrieve()
                            .bodyToMono(JsonNode.class)
                            .onErrorReturn(null)
                            .block();

                    if (metadata != null && metadata.isObject()) {
                        ObjectNode meta = (ObjectNode) metadata;
                        if (meta.has("result")) {
                            meta = (ObjectNode) meta.get("result");
                        }
                        return meta;
                    }
                    return null;
                } catch (Exception webClientEx) {
                    logger.warn("WebClient also failed for {}: {}", metadataUrl, webClientEx.getMessage());
                    return null;
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch submodel metadata for {}: {}", submodelUrl, e.getMessage());
            return null;
        }
    }
}