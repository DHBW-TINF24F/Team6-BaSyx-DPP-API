package com.team6.dpp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.team6.dpp.config.DppConfig;
import com.team6.dpp.util.DppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DppService {

    private static final Logger logger = LoggerFactory.getLogger(DppService.class);

    private final ObjectMapper mapper;
    private final RegistryService registryService;
    private final SubmodelService submodelService;

    public DppService(ObjectMapper mapper, RegistryService registryService, SubmodelService submodelService) {
        this.mapper = mapper;
        this.registryService = registryService;
        this.submodelService = submodelService;
    }

    /**
     * Finds DPP URL for a given product ID (base64 encoded AAS identifier)
     */
    public String findDppUrlForProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            return null;
        }

        // Decode base64 productId to get AAS identifier
        String aasIdentifier = DppUtils.decodeIdentifier(productId);
        if (aasIdentifier == null) {
            logger.warn("Invalid base64 productId: {}", productId);
            return null;
        }

        for (Map.Entry<String, String> entry : DppConfig.REGISTRIES.entrySet()) {
            String registry = entry.getValue();
            JsonNode shells = registryService.fetchShells(registry);
            if (shells == null || !shells.isArray()) continue;

            for (JsonNode shell : shells) {
                String shellId = registryService.extractShellId(shell);

                if (shellId != null && shellId.equals(aasIdentifier)) {
                    return registry + "/shells/" + DppUtils.encode(shellId);
                }
            }
        }

        // Fallback: try direct URL construction
        if (!DppConfig.REGISTRIES.isEmpty()) {
            String localRegistry = DppConfig.REGISTRIES.values().iterator().next();
            return localRegistry + "/shells/" + DppUtils.encode(aasIdentifier);
        }

        return null;
    }

    /**
     * Creates custom payload object for /api/v1/dpp
     */
    public ObjectNode createCustomPayload(JsonNode shell) {
        ObjectNode payload = mapper.createObjectNode();
        ObjectNode administration = payload.putObject("administration");

        ObjectNode creator = administration.putObject("creator");
        ArrayNode creatorKeys = creator.putArray("keys");

        // dynamischer Creator-Wert, falls vorhanden
        if (shell.has("creator") && shell.get("creator").has("keys") && shell.get("creator").get("keys").isArray() && shell.get("creator").get("keys").size() > 0) {
            for (JsonNode key : shell.get("creator").get("keys")) {
                creatorKeys.add(key);
            }
        } else {
            ObjectNode creatorKey = creatorKeys.addObject();
            creatorKey.put("type", "GlobalReference");
            creatorKey.put("value", "sebastian.eicke@harting.com");
        }

        ObjectNode assetInformation = administration.putObject("assetInformation");
        if (shell.has("assetInformation") && shell.get("assetInformation").isObject()) {
            ObjectNode sourceAsset = (ObjectNode) shell.get("assetInformation");
            assetInformation.setAll(sourceAsset);

            // Fill missing known fields dynamically
            if (!assetInformation.has("assetKind")) {
                assetInformation.put("assetKind", "Type");
            }
            if (!assetInformation.has("globalAssetId")) {
                assetInformation.put("globalAssetId", "https://pk.harting.com/?.20P=ZSN1");
            }
            // Keep existing defaultThumbnail if present, otherwise add a meaningful placeholder
            if (!assetInformation.has("defaultThumbnail")) {
                ObjectNode thumbnail = assetInformation.putObject("defaultThumbnail");
                thumbnail.put("contentType", "image/png");
                thumbnail.put("path", "https://via.placeholder.com/300?text=No+Image");
            }
        } else {
            assetInformation.put("note", "Placeholder: assetInformation wurde nicht gefunden");
            assetInformation.put("assetKind", "Type");

            ObjectNode thumbnail = assetInformation.putObject("defaultThumbnail");
            thumbnail.put("contentType", "image/png");
            thumbnail.put("path", "https://via.placeholder.com/300?text=No+Image");

            assetInformation.put("globalAssetId", "https://pk.harting.com/?.20P=ZSN1");
        }

        administration.put("version", shell.path("version").asText("1.0.1"));
        administration.put("id", shell.path("id").asText("https://dpp40.harting.com/shells/ZSN1"));

        if (shell.has("description")) {
            administration.set("description", shell.get("description"));
        } else if (shell.has("descriptionText")) {
            ArrayNode desc = administration.putArray("description");
            ObjectNode descItem = desc.addObject();
            descItem.put("language", "en");
            descItem.put("text", shell.path("descriptionText").asText("Placeholder: description nicht verfügbar"));
        } else {
            ArrayNode desc = administration.putArray("description");
            ObjectNode descItem = desc.addObject();
            descItem.put("language", "en");
            descItem.put("text", "Placeholder: description nicht verfügbar");
        }

        if (shell.has("displayName")) {
            administration.set("displayName", shell.get("displayName"));
        } else if (shell.has("displayNameText")) {
            ArrayNode disp = administration.putArray("displayName");
            ObjectNode displayItem = disp.addObject();
            displayItem.put("language", "en");
            displayItem.put("text", shell.path("displayNameText").asText("Placeholder: displayName nicht verfügbar"));
        } else {
            ArrayNode disp = administration.putArray("displayName");
            ObjectNode displayItem = disp.addObject();
            displayItem.put("language", "en");
            displayItem.put("text", "Placeholder: displayName nicht verfügbar");
        }

        administration.put("idShort", shell.path("idShort").asText("HARTING_AAS_ZSN1"));

        ArrayNode submodels = payload.putArray("submodels");
        ObjectNode submodelIdentifiers = payload.putObject("submodelIdentifiers");

        String[] relevantSubmodels = {
                "Digital Nameplate",
                "Handover Documentation",
                "CarbonFootprint",
                "TechnicalData",
                "Condition",
                "Material Composition",
                "Circularity"
        };

        Map<String, ObjectNode> submodelMap = new LinkedHashMap<>();
        Map<String, String> urlToIdentifier = new LinkedHashMap<>();

        for (String model : relevantSubmodels) {
            ObjectNode entry = mapper.createObjectNode();
            entry.put("type", "ExternalReference");
            entry.put("name", model);
            submodelMap.put(model, entry);
        }

        JsonNode sourceSubmodels = shell.path("submodels");
        if (sourceSubmodels.isArray()) {
            for (JsonNode sm : sourceSubmodels) {
                JsonNode keys = sm.path("keys");
                if (!keys.isArray() || keys.isEmpty()) continue;

                String submodelUrl = keys.get(0).path("value").asText("");
                if (submodelUrl.isBlank()) continue;

                String name = submodelService.mapSubmodelName(submodelUrl);
                String identifier = DppUtils.encodeIdentifier(submodelUrl);
                urlToIdentifier.put(submodelUrl, identifier);

                ObjectNode submodelEntry = mapper.createObjectNode();
                submodelEntry.put("type", "ExternalReference");

                if (name != null) {
                    submodelEntry.put("name", name);
                    submodelEntry.put("identifier", identifier);
                }

                // Baue keys Array mit dynamischen reference und payload
                ArrayNode keyArray = submodelEntry.putArray("keys");
                ObjectNode keyItem = keyArray.addObject();
                keyItem.put("type", "Submodel");
                keyItem.put("value", submodelUrl);

                // Hole $value für payload
                JsonNode payloadData = submodelService.fetchSubmodelPayload(submodelUrl);
                if (payloadData != null && !payloadData.isNull()) {
                    keyItem.set("payload", payloadData);
                } else {
                    ObjectNode placeholderPayload = mapper.createObjectNode();
                    placeholderPayload.put("note", "Placeholder: $value nicht verfügbar");
                    keyItem.set("payload", placeholderPayload);
                }

                submodels.add(submodelEntry);
                if (name != null) {
                    submodelIdentifiers.put(name, identifier);
                }
            }
        }

        // Füge nicht gefundene relevante Submodels als Platzhalter hinzu
        for (String model : relevantSubmodels) {
            if (!submodelIdentifiers.has(model)) {
                ObjectNode placeholder = mapper.createObjectNode();
                placeholder.put("type", "ExternalReference");
                placeholder.put("name", model);
                placeholder.put("identifier", "not-available");

                ArrayNode keyArray = placeholder.putArray("keys");
                ObjectNode keyItem = keyArray.addObject();
                keyItem.put("type", "Submodel");
                keyItem.put("value", "not-found");
                keyItem.put("reference", "Placeholder: Submodel nicht in Shell gefunden");

                ObjectNode placeholderPayload = mapper.createObjectNode();
                placeholderPayload.put("note", "Placeholder: Submodel nicht in Shell gefunden");
                keyItem.set("payload", placeholderPayload);

                submodels.add(placeholder);
            }
        }

        return payload;
    }
}