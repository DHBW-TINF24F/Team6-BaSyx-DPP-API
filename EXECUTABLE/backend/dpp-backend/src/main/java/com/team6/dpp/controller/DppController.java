package com.team6.dpp.controller;

import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.team6.dpp.config.DppConfig;
import com.team6.dpp.service.DppService;
import com.team6.dpp.service.RegistryService;
import com.team6.dpp.service.SubmodelService;
import com.team6.dpp.util.DppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping
public class DppController {

    private static final Logger logger = LoggerFactory.getLogger(DppController.class);

    private final ObjectMapper mapper;
    private final RestClient restClient = RestClient.create();
    private final WebClient webClient = WebClient.builder().build();
    private final DppService dppService;
    private final RegistryService registryService;
    private final SubmodelService submodelService;

    public DppController(ObjectMapper mapper, DppService dppService, RegistryService registryService, SubmodelService submodelService) {
        this.mapper = mapper;
        this.dppService = dppService;
        this.registryService = registryService;
        this.submodelService = submodelService;
    }

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
        node.put("registries", DppConfig.REGISTRIES.size());
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
        for (Map.Entry<String, String> entry : DppConfig.REGISTRIES.entrySet()) {
            String name = entry.getKey();
            String registry = entry.getValue();

            try {
                JsonNode shells = registryService.fetchShells(registry);
                if (shells == null) continue;

                ObjectNode sourceNode = mapper.createObjectNode();
                sourceNode.put("name", name);
                sourceNode.put("registry", registry);
                ArrayNode idsNode = sourceNode.putArray("dppIds");

                // Extract unique shell IDs
                for (JsonNode shell : shells) {
                    String id = registryService.extractShellId(shell);
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
            id = DppConfig.DEFAULT_DPP_URL;
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

            try {
                JsonNode shell = client.get()
                        .uri(id)
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .block();

                if (shell == null) {
                    return createErrorResponse(404, "DPP not found at given URL");
                }

                // Build complete response with custom payload
                ObjectNode root = mapper.createObjectNode();
                root.put("statusCode", 200);
                root.put("source", id);

                ObjectNode payload = dppService.createCustomPayload(shell);
                root.set("payload", payload);

                return ResponseEntity.ok(root);
            } catch (Exception e) {
                logger.error("Failed to fetch shell from URL {}: {}", id, e.getMessage());
                return createErrorResponse(500, "Failed to fetch DPP from URL");
            }
        } catch (Exception e) {
            logger.error("Failed to fetch DPP from URL {}: {}", id, e.getMessage());
            return createErrorResponse(500, "Internal server error");
        }
    }

    /**
     * Fetches submodel data by identifier
     */
    @GetMapping("/api/v1/dpp/submodels/{identifier}")
    public ResponseEntity<ObjectNode> getSubmodelByIdentifier(@PathVariable String identifier) {
        String decodedUrl = DppUtils.decodeIdentifier(identifier);
        if (decodedUrl == null || decodedUrl.isBlank()) {
            return createErrorResponse(400, "Ungültiger Submodel-Identifier");
        }

        try {
            logger.info("Fetching submodel by identifier: {}", identifier);
            JsonNode submodelData = submodelService.fetchSubmodelPayload(decodedUrl);

            ObjectNode response = mapper.createObjectNode();
            response.put("statusCode", 200);
            response.put("identifier", identifier);
            response.put("url", decodedUrl);
            response.set("data", submodelData != null ? submodelData : mapper.createObjectNode());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to fetch submodel by identifier {}: {}", identifier, e.getMessage());
            return createErrorResponse(500, "Fehler beim Abrufen des Submodels");
        }
    }
    @GetMapping("/dpp/{productId}")
    public ResponseEntity<ObjectNode> getDppByProductId(@PathVariable String productId) {
        String dppUrl = dppService.findDppUrlForProductId(productId);
        if (dppUrl == null) {
            return createErrorResponse(404, "DPP für ProduktId nicht gefunden: " + productId);
        }
        return getDppByUrl(dppUrl);
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
}