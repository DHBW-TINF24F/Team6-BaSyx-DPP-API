package com.team6.dpp.config;

import java.util.Map;

public class DppConfig {

    // Configuration: Registry endpoints
    public static final Map<String, String> REGISTRIES = Map.of(
        "harting", "https://dpp40.harting.com:8081",
        "local", "http://localhost:8080"
    );

    // Default DPP URL for /api/v1/dpp without id
    public static final String DEFAULT_DPP_URL = "https://dpp40.harting.com:8081/shells/aHR0cHM6Ly9kcHA0MC5oYXJ0aW5nLmNvbS9zaGVsbHMvWlNOMQ==";
}