package com.dpp.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64DPP {

    public static String ensureEncoding(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        if (isBase64Encoded(input)) {
            return input;
        }

        return encodeIdentifier(input);
    }

    public static String encodeIdentifier(String url) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(url.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodeIdentifier(String identifier) {
        try {
            return new String(Base64.getUrlDecoder().decode(identifier), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

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