package com.team6.dpp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
}