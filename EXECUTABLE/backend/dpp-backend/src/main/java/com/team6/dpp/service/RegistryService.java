package com.team6.dpp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.dpp.util.DppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RegistryService {

    private static final Logger logger = LoggerFactory.getLogger(RegistryService.class);

    private final RestClient restClient;
    private final ObjectMapper mapper;

    public RegistryService(ObjectMapper mapper) {
        this.restClient = RestClient.create();
        this.mapper = mapper;
    }

    /**
     * Fetches shell descriptors from registry using multiple fallback endpoints
     */
    public JsonNode fetchShells(String registry) {
        String[] endpoints = {"/shell-descriptors", "/shells", "/lookup/shells"};

        for (String endpoint : endpoints) {
            String url = registry + endpoint;
            try {
                logger.info("Trying endpoint: {}", url);
                JsonNode result = restClient.get()
                        .uri(url)
                        .retrieve()
                        .body(JsonNode.class);

                if (result == null) continue;
                if (result.has("result")) result = result.get("result");
                if (result.isArray() && result.size() > 0) {
                    logger.info("{} returned {} shells via {}", registry, result.size(), endpoint);
                    return result;
                }
            } catch (Exception e) {
                logger.warn("Endpoint {} failed: {}", url, e.getMessage());
            }
        }
        return null;
    }

    /**
     * Extracts shell ID from JSON node using standard paths
     */
    public String extractShellId(JsonNode shell) {
        if (shell.has("id")) return shell.get("id").asText();
        if (shell.has("identification") && shell.get("identification").has("id")) {
            return shell.get("identification").get("id").asText();
        }
        return null;
    }

    /**
     * Fetches a single shell by ID from registry
     */
    public JsonNode fetchShell(String registry, String shellId) {
        String url = registry + "/shells/" + DppUtils.encode(shellId);
        try {
            logger.info("Fetching shell from: {}", url);
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            logger.warn("Failed to fetch shell {} from {}: {}", shellId, registry, e.getMessage());
            return null;
        }
    }

    /**
     * Creates a new shell in the registry
     */
    public JsonNode createShell(String registry, JsonNode shellData) {
        String url = registry + "/shells";
        try {
            logger.info("Creating shell at: {}", url);
            return restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(shellData)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            logger.error("Failed to create shell at {}: {}", registry, e.getMessage());
            return null;
        }
    }

    /**
     * Updates an existing shell in the registry
     */
    public JsonNode updateShell(String registry, String shellId, JsonNode shellData) {
        String url = registry + "/shells/" + DppUtils.encode(shellId);
        try {
            logger.info("Updating shell at: {}", url);
            return restClient.put()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(shellData)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            logger.error("Failed to update shell {} at {}: {}", shellId, registry, e.getMessage());
            return null;
        }
    }

    /**
     * Deletes a shell from the registry
     */
    public boolean deleteShell(String registry, String shellId) {
        String url = registry + "/shells/" + DppUtils.encode(shellId);
        try {
            logger.info("Deleting shell at: {}", url);
            restClient.delete()
                    .uri(url)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            logger.error("Failed to delete shell {} at {}: {}", shellId, registry, e.getMessage());
            return false;
        }
    }

    /**
     * Creates a new submodel in the registry
     */
    public JsonNode createSubmodel(String registry, JsonNode submodelData) {
        String url = registry + "/submodels";
        try {
            logger.info("Creating submodel at: {}", url);
            return restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(submodelData)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            logger.error("Failed to create submodel at {}: {}", registry, e.getMessage());
            return null;
        }
    }
}