package com.team6.dpp.controller;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping
public class DppController {

    private static final Logger logger = LoggerFactory.getLogger(DppController.class);

    // Configuration: Registry endpoints
    private static final Map<String, String> REGISTRIES = Map.of(
        "harting", "https://dpp40.harting.com:8081",
        "local", "http://localhost:8081"
    );

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestClient restClient = RestClient.create();
    private final WebClient webClient = WebClient.builder().build();

    // ================================================================================
    // HEALTH CHECK
    // ================================================================================
    
    /**
     * Health check endpoint returning service status and registry count
     */
    @GetMapping("/api/v1/dpp/health")
    public ResponseEntity<ObjectNode> health() {
        ObjectNode node = mapper.createObjectNode();
        node.put("status", "UP");
        node.put("registries", REGISTRIES.size());
        return ResponseEntity.ok(node);
    }

    // ================================================================================
    // DISCOVERY ENDPOINTS (Legacy /list functionality)
    // ================================================================================

    /**
     * Lists DPP shell IDs from configured registries with optional limit
     */
    @GetMapping("/api/v1/dpp/list")
    public ResponseEntity<ObjectNode> list(@RequestParam(defaultValue = "10") int limit) {
        ObjectNode response = mapper.createObjectNode();
        response.put("statusCode", 200);

        ObjectNode payload = response.putObject("payload");
        ArrayNode localArray = payload.putArray("local");
        ArrayNode externalArray = payload.putArray("external");

        Set<String> uniqueIds = new HashSet<>();
        int total = 0;

        // Query each configured registry
        for (Map.Entry<String, String> entry : REGISTRIES.entrySet()) {
            String name = entry.getKey();
            String registry = entry.getValue();

            try {
                JsonNode shells = fetchShells(registry);
                if (shells == null) continue;

                ObjectNode sourceNode = mapper.createObjectNode();
                sourceNode.put("name", name);
                sourceNode.put("registry", registry);
                ArrayNode idsNode = sourceNode.putArray("dppIds");

                // Extract unique shell IDs
                for (JsonNode shell : shells) {
                    String id = extractShellId(shell);
                    if (id == null) continue;

                    if (uniqueIds.add(id)) {
                        idsNode.add(id);
                        total++;
                        if (total >= limit) break;
                    }
                }

                // Add non-empty results to appropriate array
                if (idsNode.size() > 0) {
                    if ("local".equals(name)) {
                        localArray.addAll(idsNode);
                    } else {
                        externalArray.add(sourceNode);
                    }
                }

            } catch (Exception e) {
                logger.error("Registry {} failed: {}", registry, e.getMessage());
            }
        }

        payload.put("total", total);
        return ResponseEntity.ok(response);
    }

    /**
     * Fetches shell descriptors from registry using multiple fallback endpoints
     */
    private JsonNode fetchShells(String registry) {
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
    private String extractShellId(JsonNode shell) {
        if (shell.has("id")) return shell.get("id").asText();
        if (shell.has("identification") && shell.get("identification").has("id")) {
            return shell.get("identification").get("id").asText();
        }
        return null;
    }

    // ================================================================================
    // DPP CRUD OPERATIONS
    // ================================================================================

    @PostMapping("/dpps")
    public ResponseEntity<JsonNode> createDpp(@RequestBody JsonNode dpp) {
        ObjectNode response = mapper.createObjectNode();
        response.put("dppId", dpp.path("dppId").asText());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/dpps/{dppId}")
    public ResponseEntity<JsonNode> readDppById(@PathVariable String dppId) {
        ObjectNode dpp = mapper.createObjectNode();
        dpp.put("dppId", dppId);
        dpp.put("productId", "example-product-id");
        return ResponseEntity.ok(dpp);
    }

    @PatchMapping("/dpps/{dppId}")
    public ResponseEntity<JsonNode> updateDppById(@PathVariable String dppId, @RequestBody JsonNode patch) {
        ObjectNode dpp = mapper.createObjectNode();
        dpp.put("dppId", dppId);
        dpp.setAll((ObjectNode) patch);
        return ResponseEntity.ok(dpp);
    }

    @DeleteMapping("/dpps/{dppId}")
    public ResponseEntity<Void> deleteDppById(@PathVariable String dppId) {
        return ResponseEntity.noContent().build();
    }

    // ================================================================================
    // DPP BY PRODUCT ID ENDPOINTS
    // ================================================================================

    @GetMapping("/dppsByProductId/{productId}")
    public ResponseEntity<JsonNode> readDppByProductId(@PathVariable String productId) {
        ObjectNode dpp = mapper.createObjectNode();
        dpp.put("dppId", "example-dpp-id");
        dpp.put("productId", productId);
        return ResponseEntity.ok(dpp);
    }

    @GetMapping("/dppsByProductIdAndDate/{productId}")
    public ResponseEntity<JsonNode> readDppVersionByProductIdAndDate(
            @PathVariable String productId,
            @RequestParam String date) {
        ObjectNode dpp = mapper.createObjectNode();
        dpp.put("dppId", "example-dpp-id");
        dpp.put("productId", productId);
        dpp.put("versionDate", date);
        return ResponseEntity.ok(dpp);
    }

    @PostMapping("/dppsByProductIds")
    public ResponseEntity<JsonNode> readDppIdsByProductIds(@RequestBody JsonNode body,
                                                          @RequestParam(required = false) Integer limit,
                                                          @RequestParam(required = false) String cursor) {
        ArrayNode dppIds = mapper.createArrayNode();
        if (body.has("productIds")) {
            for (JsonNode id : body.get("productIds")) {
                dppIds.add("dpp-for-" + id.asText());
            }
        }
        ObjectNode response = mapper.createObjectNode();
        response.set("dppIds", dppIds);
        return ResponseEntity.ok(response);
    }

    // ================================================================================
    // REGISTRY OPERATIONS
    // ================================================================================

    @PostMapping("/registerDPP")
    public ResponseEntity<JsonNode> postNewDppToRegistry(@RequestBody JsonNode body) {
        ObjectNode response = mapper.createObjectNode();
        response.put("registryIdentifier", "registry-" + UUID.randomUUID());
        return ResponseEntity.status(201).body(response);
    }

    // ================================================================================
    // DPP FETCH BY URL
    // ================================================================================

    /**
     * Fetches complete DPP (shell + submodels) directly from provided URL
     */
    @GetMapping("/api/v1/dpp")
    public ResponseEntity<ObjectNode> getDppByUrl(@RequestParam String id) {
        // Validate URL format
        if (!id.startsWith("http")) {
            ObjectNode error = mapper.createObjectNode();
            error.put("statusCode", 400);
            error.put("error", "Parameter 'id' muss eine vollständige URL sein");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            logger.info("Fetching DPP directly from URL: {}", id);

            // Fetch shell descriptor with redirects
            WebClient client = WebClient.builder()
                    .defaultHeader("Accept", "application/json")
                    .build();

            ClientResponse response = client.get()
                    .uri(id)
                    .exchange()
                    .block();

            if (response == null) {
                return createErrorResponse(404, "DPP not found at given URL");
            }

            MediaType contentType = response.headers().contentType().orElse(MediaType.APPLICATION_JSON);
            if (!MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
                return createErrorResponse(415, "Content-Type nicht JSON: " + contentType.toString());
            }

            JsonNode shell = response.bodyToMono(JsonNode.class).block();

            // Build complete response
            ObjectNode root = mapper.createObjectNode();
            root.put("statusCode", 200);
            root.put("source", id);

            ObjectNode payload = root.putObject("payload");
            copyShellFields(shell, payload);

            // Fetch and add submodels
            ArrayNode submodelsArray = payload.putArray("submodels");
            JsonNode submodels = shell.get("submodels");
            if (submodels != null && submodels.isArray()) {
                for (JsonNode sm : submodels) {
                    JsonNode keys = sm.get("keys");
                    if (keys == null || keys.isEmpty()) continue;
                    String submodelUrl = keys.get(0).get("value").asText();
                    processSubmodelWebClient(submodelUrl, submodelsArray);
                }
            }

            return ResponseEntity.ok(root);

        } catch (Exception e) {
            logger.error("Failed to fetch DPP from URL {}: {}", id, e.getMessage());
            return createErrorResponse(500, "Internal server error");
        }
    }

    /**
     * Fetches submodel data via WebClient and adds to target array
     */
    private void processSubmodelWebClient(String submodelUrl, ArrayNode target) {
        try {
            logger.info("Fetching submodel: {}", submodelUrl);

            JsonNode value = webClient.get()
                    .uri(submodelUrl)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            ObjectNode node = mapper.createObjectNode();
            node.put("id", submodelUrl);
            node.set("data", value);
            target.add(node);

        } catch (Exception e) {
            logger.warn("Failed to fetch submodel {}: {}", submodelUrl, e.getMessage());
        }
    }

    /**
     * Legacy submodel processing method (RestClient)
     */
    private void processSubmodel(String submodelUrl, ArrayNode target) {
        try {
            JsonNode value = restClient.get()
                    .uri(submodelUrl)
                    .retrieve()
                    .body(JsonNode.class);

            ObjectNode node = mapper.createObjectNode();
            node.put("id", submodelUrl);
            node.set("data", value);
            target.add(node);

        } catch (Exception e) {
            logger.warn("Submodel failed: {}", e.getMessage());
        }
    }

    /**
     * Copies standard shell fields to target node
     */
    private void copyShellFields(JsonNode source, ObjectNode target) {
        String[] fields = {"id", "idShort", "description", "displayName", "assetInformation"};
        for (String field : fields) {
            if (source.has(field)) {
                target.set(field, source.get(field));
            }
        }
    }

    /**
     * Creates standardized error response
     */
    private ResponseEntity<ObjectNode> createErrorResponse(int statusCode, String errorMessage) {
        ObjectNode error = mapper.createObjectNode();
        error.put("statusCode", statusCode);
        error.put("error", errorMessage);
        return ResponseEntity.status(statusCode).body(error);
    }

    /**
     * Base64 URL encoding utility
     */
    private String encode(String input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }
}