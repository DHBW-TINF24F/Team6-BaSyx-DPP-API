package com.team6.dpp.controller;

import java.text.DateFormat;
import java.util.ArrayList;

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

    // lifecycle API

    @PostMapping("/dpps")
    public String CreateDPPS(@PathVariable String productId) {
        return "Null";
        // immer json returnen
    }
   
    @GetMapping("/dpps/{dppID}")
    public String ReadDPPById(@PathVariable String dppID) {
        return "Null";
        // returned JSON
    }

    @PatchMapping("/dpps/{dppID}")
    public String UpdateDPP(@PathVariable String dppID) {
        return "Null";
        // returned JSON
    }

    @DeleteMapping("/dpps/{dppID}")
    public String DeleteDPPById(@PathVariable String dppID) {
        return "Null";
        // returned JSON
    }

    @GetMapping("/dppsByProductId/{productId}")
    public String ReadDPPByProductId(@PathVariable String productId) {
        return "Null";
        // returned JSON
    }

    @GetMapping("/dppsByProductIdAndDate/{productId}?={date}")
    public String ReadDPPVersionByProductIdAndDate(@PathVariable String productId, @RequestParam DateFormat date) {
        return "Null";
        // returned JSON
    }

    @PostMapping("/dppsByProductIds/{productIds}")
    public String ReadDPPIdsByProductIds(@RequestBody String productIds) {
        return "Null";
        // returned JSON
    }


    // registry API

    @PostMapping("/registerDpp")
    public String PostNewDPPToRegistry() {
        return "Null";
        // returned JSON
    }


    // DPP Finegranular Lifecycle API

    @GetMapping("/dpps/{dppID}/collections/{elementId}")
    public String ReadDataElementCollection(@PathVariable String dppID, @PathVariable String elementId) {
        return "Null";
        // returned JSON
    }

    @PatchMapping("/dpps/{dppID}/collections/{elementId}")
    public String UpdateDataElementCollection(@PathVariable String dppID, @PathVariable String elementId) {
        return "Null";
        // returned JSON
    }

    @GetMapping("/dpps/{dppID}/elements/{elementPath}")
    public String ReadElement(@PathVariable String dppID, @PathVariable String elementPath) {
        return "Null";
        // returned JSON
    }

    @PatchMapping("/dpps/{dppID}/elements/{elementPath}")
        public String UpdateElement(@PathVariable String dppID, @PathVariable String elementPath) {
        return "Null";
        // returned JSON
    }
}