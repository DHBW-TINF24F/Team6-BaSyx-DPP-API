package com.dpp.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64DPP {

    /**
     * Checks if a string is already Base64 encoded. 
     * If not, it encodes it to a URL-safe Base64 string.
     */
    public static String ensureEncoding(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        if (isBase64Encoded(input)) {
            return input;
        }

        return encodeIdentifier(input);
    }

    /**
     * Encodes a string to URL-safe Base64 without padding.
     */
    public static String encodeIdentifier(String url) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(url.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes a Base64 identifier back to the original string.
     */
    public static String decodeIdentifier(String identifier) {
        try {
            return new String(Base64.getUrlDecoder().decode(identifier), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Helper to determine if a string is valid Base64.
     */
    private static boolean isBase64Encoded(String input) {
        try {
            // Attempt to decode. If it fails, it wasn't valid Base64.
            Base64.getUrlDecoder().decode(input);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}