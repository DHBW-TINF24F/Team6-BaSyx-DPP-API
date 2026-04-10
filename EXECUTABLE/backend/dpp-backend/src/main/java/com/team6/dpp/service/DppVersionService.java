package com.team6.dpp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service für die Verwaltung von DPP-Versionsnummern.
 * dppID = productID + "#" + versionNumber
 */
@Service
public class DppVersionService {

    private static final Logger logger = LoggerFactory.getLogger(DppVersionService.class);

    // In-Memory Counter für jede productID
    private final Map<String, AtomicInteger> versionCounters = new ConcurrentHashMap<>();

    /**
     * Generiert die nächste dppID für eine productID
     * Format: productID#versionNumber
     *
     * @param productId base64-encoded AAS Identifier
     * @return generierte dppID mit Versionsnummer
     */
    public String getNextDppId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId darf nicht null oder leer sein");
        }

        AtomicInteger counter = versionCounters.computeIfAbsent(productId, k -> new AtomicInteger(0));
        int nextVersion = counter.incrementAndGet();
        String dppId = formatDppId(productId, nextVersion);

        logger.info("Generated DPP ID {} for product {}, version {}", dppId, productId, nextVersion);
        return dppId;
    }

    /**
     * Formatiert produktID + Versionsnummer zu dppID
     *
     * @param productId base64-encoded AAS Identifier
     * @param versionNumber Versionsnummer (1, 2, 3, ...)
     * @return formatierte dppID
     */
    public String formatDppId(String productId, int versionNumber) {
        return productId + "#" + versionNumber;
    }

    /**
     * Parsed dppID in Komponenten (productId + versionNumber)
     *
     * @param dppId dppID zum Parsen
     * @return DppIdParts mit productId und versionNumber
     */
    public DppIdParts parseDppId(String dppId) {
        if (dppId == null || dppId.isBlank()) {
            throw new IllegalArgumentException("dppId darf nicht null oder leer sein");
        }

        int lastHash = dppId.lastIndexOf('#');
        if (lastHash == -1 || lastHash == dppId.length() - 1) {
            // Fallback: dppId ist eine productId ohne Versionsnummer
            logger.warn("dppId {} has no version number, treating as version 1", dppId);
            return new DppIdParts(dppId, 1);
        }

        try {
            String productId = dppId.substring(0, lastHash);
            int versionNumber = Integer.parseInt(dppId.substring(lastHash + 1));
            return new DppIdParts(productId, versionNumber);
        } catch (NumberFormatException e) {
            logger.error("Invalid dppId format: {}", dppId);
            throw new IllegalArgumentException("Ungültige dppID Format: " + dppId, e);
        }
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
     * Extrahiert nur die Versionsnummer aus einer dppID
     *
     * @param dppId dppID
     * @return Versionsnummer
     */
    public int extractVersionNumber(String dppId) {
        return parseDppId(dppId).versionNumber;
    }

    /**
     * Hilfklasse für geparste dppID
     */
    public static class DppIdParts {
        public final String productId;
        public final int versionNumber;

        public DppIdParts(String productId, int versionNumber) {
            this.productId = productId;
            this.versionNumber = versionNumber;
        }

        @Override
        public String toString() {
            return "DppIdParts{" +
                    "productId='" + productId + '\'' +
                    ", versionNumber=" + versionNumber +
                    '}';
        }
    }
}
