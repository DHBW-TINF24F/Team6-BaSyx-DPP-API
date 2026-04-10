package com.team6.dpp.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service für die Verwaltung von DPP-Versionen basierend auf Shell-Werten.
 * dppID = productID + "#" + versionValue
 * Der versionValue wird aus der Shell extrahiert (z.B. version, timestamp).
 */
@Service
public class DppVersionService {

    private static final Logger logger = LoggerFactory.getLogger(DppVersionService.class);

    /**
     * Extrahiert die Versionsnummer aus einer Shell und generiert die dppID
     * Versucht folgende Quellen (in dieser Reihenfolge):
     * 1. shell.administration.version
     * 2. shell.version
     * 3. shell.modifiedDate
     * 4. Aktueller Timestamp
     *
     * @param productId base64-encoded AAS Identifier
     * @param shell die AAS Shell
     * @return generierte dppID mit Version
     */
    public String generateDppIdFromShell(String productId, JsonNode shell) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId darf nicht null oder leer sein");
        }
        if (shell == null) {
            throw new IllegalArgumentException("shell darf nicht null sein");
        }

        String versionValue = extractVersionValue(shell);
        String dppId = formatDppId(productId, versionValue);

        logger.info("Generated DPP ID {} from shell version: {}", dppId, versionValue);
        return dppId;
    }

    /**
     * Extrahiert den Versionswert aus der Shell
     * Versucht mehrere Quellen in dieser Reihenfolge:
     * - shell.administration.version
     * - shell.version
     * - shell.modifiedDate
     * - Aktueller ISO-Timestamp als Fallback
     *
     * @param shell die AAS Shell
     * @return Versionswert als String
     */
    public String extractVersionValue(JsonNode shell) {
        // 1. Versuche shell.administration.version
        if (shell.has("administration")) {
            JsonNode admin = shell.get("administration");
            if (admin.has("version")) {
                String version = admin.get("version").asText(null);
                if (version != null && !version.isBlank()) {
                    return version;
                }
            }
        }

        // 2. Versuche shell.version
        if (shell.has("version")) {
            String version = shell.get("version").asText(null);
            if (version != null && !version.isBlank()) {
                return version;
            }
        }

        // 3. Versuche shell.modifiedDate
        if (shell.has("modifiedDate")) {
            String modifiedDate = shell.get("modifiedDate").asText(null);
            if (modifiedDate != null && !modifiedDate.isBlank()) {
                return modifiedDate;
            }
        }

        // 4. Fallback: Aktueller Timestamp
        String timestamp = Instant.now().toString();
        logger.warn("No version found in shell, using timestamp as version: {}", timestamp);
        return timestamp;
    }

    /**
     * Formatiert productID + Versionswert zu dppID
     *
     * @param productId base64-encoded AAS Identifier
     * @param versionValue Versionswert (z.B. "1.0.1", timestamp)
     * @return formatierte dppID
     */
    public String formatDppId(String productId, String versionValue) {
        return productId + "@" + versionValue;
    }

    /**
     * Parsed dppID in Komponenten (productId + versionValue)
     *
     * @param dppId dppID zum Parsen
     * @return DppIdParts mit productId und versionValue
     */
    public DppIdParts parseDppId(String dppId) {
        if (dppId == null || dppId.isBlank()) {
            throw new IllegalArgumentException("dppId darf nicht null oder leer sein");
        }

        int lastAt = dppId.lastIndexOf('@');
        if (lastAt == -1 || lastAt == dppId.length() - 1) {
            // Fallback: dppId ist eine productId ohne Versionswert
            logger.warn("dppId {} has no version value, treating as version 'unknown'", dppId);
            return new DppIdParts(dppId, "unknown");
        }

        String productId = dppId.substring(0, lastAt);
        String versionValue = dppId.substring(lastAt + 1);
        return new DppIdParts(productId, versionValue);
    }

    /**
     * Extrahiert nur die productID aus einer dppID
     *
     * @param dppId dppID
     * @return productID (base64-encoded AAS Identifier)
     */
    public String extractProductId(String dppId) {
        return parseDppId(dppId).productId;
    }

    /**
     * Extrahiert nur den Versionswert aus einer dppID
     *
     * @param dppId dppID
     * @return Versionswert
     */
    public String extractVersionValue(String dppId) {
        return parseDppId(dppId).versionValue;
    }

    /**
     * Hilfklasse für geparste dppID
     */
    public static class DppIdParts {
        public final String productId;
        public final String versionValue;

        public DppIdParts(String productId, String versionValue) {
            this.productId = productId;
            this.versionValue = versionValue;
        }

        @Override
        public String toString() {
            return "DppIdParts{" +
                    "productId='" + productId + '\'' +
                    ", versionValue='" + versionValue + '\'' +
                    '}';
        }
    }
}
