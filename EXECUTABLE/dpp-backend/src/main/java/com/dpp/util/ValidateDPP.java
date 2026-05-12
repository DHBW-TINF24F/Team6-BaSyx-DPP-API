package com.dpp.util;


import com.fasterxml.jackson.databind.JsonNode;

public class ValidateDPP {

    public static boolean validateJsonTillFirstEntry(JsonNode dpp) {

        // 1. Check top-level 'shell' and 'id'
        if (!dpp.has("shell") || !dpp.get("shell").has("id")) {
            return false;
        }

        JsonNode dppsArray = dpp.get("shell").path("dpps");
        
        // 2. Validate dpps array exists and is not empty
        if (!dppsArray.isArray() || dppsArray.isEmpty()) {
            return false;
        }

        // 3. Validate the first dpp entry structure
        JsonNode firstDpp = dppsArray.get(0);
        String[] requiredDppFields = {"productId", "version"};
        
        for (String field : requiredDppFields) {
            if (!firstDpp.has(field)) {
                return false;
            }
        }
        return true;
    }
}
