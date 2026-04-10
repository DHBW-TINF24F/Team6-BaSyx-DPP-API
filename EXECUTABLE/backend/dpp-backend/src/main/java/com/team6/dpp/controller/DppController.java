package com.team6.dpp.controller;

import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.team6.dpp.config.DppConfig;
import com.team6.dpp.service.DppService;
import com.team6.dpp.service.DppVersionService;
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
    private final DppVersionService dppVersionService;

    public DppController(ObjectMapper mapper, DppService dppService, RegistryService registryService, SubmodelService submodelService, DppVersionService dppVersionService) {
        this.mapper = mapper;
        this.dppService = dppService;
        this.registryService = registryService;
        this.submodelService = submodelService;
        this.dppVersionService = dppVersionService;
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
    public ResponseEntity<JsonNode> createDpp(@RequestBody JsonNode dpp, @RequestParam String productId) {
        try {
            // Assume the first registry for creation
            String registry = DppConfig.REGISTRIES.values().iterator().next();
            JsonNode createdShell = registryService.createShell(registry, dpp);
            if (createdShell == null) {
                return ResponseEntity.status(500).body(mapper.createObjectNode().put("error", "Failed to create DPP"));
            }

            // Generate next dppID with automatic versioning
            String dppId = dppVersionService.getNextDppId(productId);

            ObjectNode response = mapper.createObjectNode();
            response.put("dppId", dppId);
            response.put("productId", productId);
            response.put("versionNumber", dppVersionService.extractVersionNumber(dppId));
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            logger.error("Failed to create DPP: {}", e.getMessage());
            return ResponseEntity.status(500).body(mapper.createObjectNode().put("error", "Internal server error"));
        }
    }

    @GetMapping("/dpps/{dppId}")
    public ResponseEntity<JsonNode> readDppById(@PathVariable String dppId) {
        try {
            // Parse dppID to extract productID and versionNumber
            DppVersionService.DppIdParts parts = dppVersionService.parseDppId(dppId);
            String productId = parts.productId;
            int versionNumber = parts.versionNumber;

            // Decode productId to get AAS identifier
            String aasId = DppUtils.decodeIdentifier(productId);
            if (aasId == null) {
                aasId = productId; // Assume it's already the AAS ID
            }

            // Find registry and fetch shell
            for (String registry : DppConfig.REGISTRIES.values()) {
                JsonNode shell = registryService.fetchShell(registry, aasId);
                if (shell != null) {
                    ObjectNode dpp = mapper.createObjectNode();
                    dpp.put("dppId", dppId);
                    dpp.put("productId", productId);
                    dpp.put("versionNumber", versionNumber);
                    dpp.set("shell", shell);
                    dpp.set("payload", dppService.createCustomPayload(shell));
                    return ResponseEntity.ok(dpp);
                }
            }
            return ResponseEntity.status(404).body(mapper.createObjectNode().put("error", "DPP not found"));
        } catch (Exception e) {
            logger.error("Failed to read DPP {}: {}", dppId, e.getMessage());
            return ResponseEntity.status(500).body(mapper.createObjectNode().put("error", "Internal server error"));
        }
    }

    @PatchMapping("/dpps/{dppId}")
    public ResponseEntity<JsonNode> updateDppById(@PathVariable String dppId, @RequestBody JsonNode patch) {
        try {
            // Parse dppID to extract productID and versionNumber
            DppVersionService.DppIdParts parts = dppVersionService.parseDppId(dppId);
            String productId = parts.productId;
            int versionNumber = parts.versionNumber;

            // Decode productId to get AAS identifier
            String aasId = DppUtils.decodeIdentifier(productId);
            if (aasId == null) {
                aasId = productId;
            }

            // Find registry and update shell
            for (String registry : DppConfig.REGISTRIES.values()) {
                JsonNode existingShell = registryService.fetchShell(registry, aasId);
                if (existingShell != null) {
                    JsonNode updatedShell = registryService.updateShell(registry, aasId, patch);
                    if (updatedShell != null) {
                        ObjectNode dpp = mapper.createObjectNode();
                        dpp.put("dppId", dppId);
                        dpp.put("productId", productId);
                        dpp.put("versionNumber", versionNumber);
                        dpp.set("shell", updatedShell);
                        return ResponseEntity.ok(dpp);
                    }
                }
            }
            return ResponseEntity.status(404).body(mapper.createObjectNode().put("error", "DPP not found"));
        } catch (Exception e) {
            logger.error("Failed to update DPP {}: {}", dppId, e.getMessage());
            return ResponseEntity.status(500).body(mapper.createObjectNode().put("error", "Internal server error"));
        }
    }

    @DeleteMapping("/dpps/{dppId}")
    public ResponseEntity<Void> deleteDppById(@PathVariable String dppId) {
        try {
            // Parse dppID to extract productID
            DppVersionService.DppIdParts parts = dppVersionService.parseDppId(dppId);
            String productId = parts.productId;

            // Decode productId to get AAS identifier
            String aasId = DppUtils.decodeIdentifier(productId);
            if (aasId == null) {
                aasId = productId;
            }

            // Find registry and delete shell
            for (String registry : DppConfig.REGISTRIES.values()) {
                if (registryService.deleteShell(registry, aasId)) {
                    return ResponseEntity.noContent().build();
                }
            }
            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            logger.error("Failed to delete DPP {}: {}", dppId, e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // ================================================================================
    // DPP BY PRODUCT ID ENDPOINTS
    // ================================================================================

    @GetMapping("/dppsByProductId/{productId}")
    public ResponseEntity<JsonNode> readDppByProductId(@PathVariable String productId) {
        try {
            String dppUrl = dppService.findDppUrlForProductId(productId);
            if (dppUrl == null) {
                return ResponseEntity.status(404).body(mapper.createObjectNode().put("error", "DPP für ProduktId nicht gefunden: " + productId));
            }
            ResponseEntity<ObjectNode> response = getDppByUrl(dppUrl);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            logger.error("Failed to read DPP by productId {}: {}", productId, e.getMessage());
            return ResponseEntity.status(500).body(mapper.createObjectNode().put("error", "Internal server error"));
        }
    }

    @GetMapping("/dppsByProductIdAndDate/{productId}")
    public ResponseEntity<JsonNode> readDppVersionByProductIdAndDate(
            @PathVariable String productId,
            @RequestParam String date) {
        try {
            // For now, return the current DPP with version info
            String dppUrl = dppService.findDppUrlForProductId(productId);
            if (dppUrl == null) {
                return ResponseEntity.status(404).body(mapper.createObjectNode().put("error", "DPP für ProduktId nicht gefunden: " + productId));
            }
            ResponseEntity<ObjectNode> response = getDppByUrl(dppUrl);
            if (response.getBody() != null) {
                response.getBody().put("versionDate", date);
            }
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            logger.error("Failed to read DPP version by productId {} and date {}: {}", productId, date, e.getMessage());
            return ResponseEntity.status(500).body(mapper.createObjectNode().put("error", "Internal server error"));
        }
    }

    @PostMapping("/dppsByProductIds")
    public ResponseEntity<JsonNode> readDppIdsByProductIds(@RequestBody JsonNode body,
                                                          @RequestParam(required = false) Integer limit,
                                                          @RequestParam(required = false) String cursor) {
        try {
            ArrayNode dppIds = mapper.createArrayNode();
            if (body.has("productIds") && body.get("productIds").isArray()) {
                int count = 0;
                int maxLimit = limit != null ? limit : Integer.MAX_VALUE;
                for (JsonNode productIdNode : body.get("productIds")) {
                    if (count >= maxLimit) break;
                    String productId = productIdNode.asText();
                    String dppUrl = dppService.findDppUrlForProductId(productId);
                    if (dppUrl != null) {
                        // Generate DPP IDs with versioning (use version 1 as default)
                        String dppId = dppVersionService.formatDppId(productId, 1);
                        dppIds.add(dppId);
                        count++;
                    }
                }
            }
            ObjectNode response = mapper.createObjectNode();
            response.set("dppIds", dppIds);
            if (cursor != null) {
                response.put("cursor", cursor);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to read DPP IDs by productIds: {}", e.getMessage());
            return ResponseEntity.status(500).body(mapper.createObjectNode().put("error", "Internal server error"));
        }
    }

    // ================================================================================
    // REGISTRY OPERATIONS
    // ================================================================================

    @PostMapping("/registerDPP")
    public ResponseEntity<JsonNode> postNewDppToRegistry(@RequestBody JsonNode body) {
        try {
            // Assume the body contains the shell data
            String registry = DppConfig.REGISTRIES.values().iterator().next(); // Use first registry
            JsonNode createdShell = registryService.createShell(registry, body);
            if (createdShell == null) {
                return ResponseEntity.status(500).body(mapper.createObjectNode().put("error", "Failed to register DPP"));
            }
            ObjectNode response = mapper.createObjectNode();
            response.put("registryIdentifier", registryService.extractShellId(createdShell));
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            logger.error("Failed to register DPP: {}", e.getMessage());
            return ResponseEntity.status(500).body(mapper.createObjectNode().put("error", "Internal server error"));
        }
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