package com.team6.dpp.config;

import java.util.Map;

public class DppConfig {

    // Configuration: Registry endpoints
    public static final Map<String, String> REGISTRIES = Map.of(
        "harting", "http://localhost:8081",
        "local", "http://localhost:8081"
    );

    // Default DPP URL for /api/v1/dpp without id
    public static final String DEFAULT_DPP_URL = "http://localhost:8081"; // Example URL, replace with actual default
}