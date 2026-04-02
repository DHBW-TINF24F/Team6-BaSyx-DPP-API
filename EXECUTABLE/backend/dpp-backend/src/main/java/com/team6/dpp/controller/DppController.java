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

    // Default DPP URL for /api/v1/dpp without id
    private static final String DEFAULT_DPP_URL = "https://dpp40.harting.com:8081/shells/aHR0cHM6Ly9kcHA0MC5oYXJ0aW5nLmNvbS9zaGVsbHMvWlNOMQ==";

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
    public ResponseEntity<ObjectNode> getDppByUrl(@RequestParam(required = false) String id) {
        // Use default URL when no id query param is provided
        if (id == null || id.isBlank()) {
            id = DEFAULT_DPP_URL;
        }

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

            // Build complete response with custom payload
            ObjectNode root = mapper.createObjectNode();
            root.put("statusCode", 200);
            root.put("source", id);

            ObjectNode payload = createCustomPayload(shell);
            root.set("payload", payload);

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
     * Creates custom payload object for /api/v1/dpp
     */
    private ObjectNode createCustomPayload(JsonNode shell) {
        ObjectNode payload = mapper.createObjectNode();
        ObjectNode administration = payload.putObject("administration");

        ObjectNode creator = administration.putObject("creator");
        ArrayNode creatorKeys = creator.putArray("keys");
        ObjectNode creatorKey = creatorKeys.addObject();
        creatorKey.put("type", "GlobalReference");
        creatorKey.put("value", "sebastian.eicke@harting.com");

        ObjectNode assetInformation = administration.putObject("assetInformation");
        if (shell.has("assetInformation")) {
            assetInformation.setAll((ObjectNode) shell.get("assetInformation"));
        } else {
            assetInformation.put("note", "Placeholder: assetInformation wurde nicht gefunden");
            assetInformation.put("assetKind", "Type");
            assetInformation.put("defaultThumbnail", "Placeholder");
            assetInformation.put("globalAssetId", "https://pk.harting.com/?.20P=ZSN1");
        }

        administration.put("version", "1.0.1");
        administration.put("id", shell.path("id").asText("https://dpp40.harting.com/shells/ZSN1"));

        if (shell.has("description")) {
            administration.set("description", shell.get("description"));
        } else {
            ArrayNode desc = administration.putArray("description");
            ObjectNode descItem = desc.addObject();
            descItem.put("language", "en");
            descItem.put("text", "Placeholder: description nicht verfügbar");
        }

        if (shell.has("displayName")) {
            administration.set("displayName", shell.get("displayName"));
        } else {
            ArrayNode disp = administration.putArray("displayName");
            ObjectNode displayItem = disp.addObject();
            displayItem.put("language", "en");
            displayItem.put("text", "Placeholder: displayName nicht verfügbar");
        }

        administration.put("idShort", shell.path("idShort").asText("HARTING_AAS_ZSN1"));

        // Submodels werden hier vorerst nicht geladen - Platzhalter einfügen.
        ArrayNode submodels = payload.putArray("submodels");
        ObjectNode placeholderSubmodel = mapper.createObjectNode();
        placeholderSubmodel.put("note", "Placeholder: Submodel-Logik kommt später als eigene Funktion");
        submodels.add(placeholderSubmodel);

        return payload;
    }

    private JsonNode fetchSubmodelPayload(String submodelUrl) {
        try {
            return webClient.get()
                    .uri(submodelUrl)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (Exception e) {
            logger.warn("Failed to fetch submodel payload for {}: {}", submodelUrl, e.getMessage());
            return null;
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