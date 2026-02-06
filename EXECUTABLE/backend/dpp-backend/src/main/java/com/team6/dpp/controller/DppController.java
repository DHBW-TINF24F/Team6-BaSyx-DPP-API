package com.team6.dpp.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dpp")
public class DppController {
    
    @GetMapping("/health")
    public String health() {
        return "DPP Backend ready!";
    }
    
    @GetMapping("/{productId}")
    public String getDpp(@PathVariable String productId) {
        return "{\"productId\":\"" + productId + "\", \"status\":\"active\"}";
    }
    
    @PostMapping
    public String createDpp(@RequestBody String dppData) {
        return "DPP created: " + dppData;
    }
}
