package com.team6.dpp.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DppUtils {

    /**
     * Base64 URL encoding utility
     */
    public static String encode(String input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encodes a URL into a Base64 identifier
     */
    public static String encodeIdentifier(String url) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(url.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes a Base64 identifier back to URL
     */
    public static String decodeIdentifier(String identifier) {
        try {
            return new String(Base64.getUrlDecoder().decode(identifier), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}