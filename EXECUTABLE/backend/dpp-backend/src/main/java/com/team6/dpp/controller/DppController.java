package com.team6.dpp.controller;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.Matcher;
import java.util.Base64;
import java.util.regex.Pattern;


import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;



@RestController
@RequestMapping("/api/v1/dpp")
public class DppController {

    private String HostUrl = "https://dpp40.harting.com:8081";
    
    @GetMapping("/health")
    public String health() {
        return "DPP Backend ready!";
    }
    
    /* productId is the AAS Identifyer
    X 1. call /shells/{aasidentifier}
                ^
        Simulieren mit: https://dpp40.harting.com:8081/shells/aHR0cHM6Ly9kcHA0MC5oYXJ0aW5nLmNvbS9zaGVsbHMvWlNOMQ
    2. find the 7 relevant submodles
    3. create a Dpp return Object
    */ 
    @GetMapping("/{productId}")
    public String getDpp(@PathVariable String productId) {
        String Base64ProductId = Base64.getEncoder().encodeToString(productId);


        // do the call to /shells
        RestClient restClient = RestClient.create();
 
        JsonNode result = restClient.get().uri(HostUrl + "/shells/" + Base64ProductId).retrieve().body(JsonNode.class);
        JsonNode submodles = result.get("submodels");
        
        ObjectNode n = new ObjectNode();
        n.putPOJO()
        
        for (JsonNode submodel: submodels) {
            String SubmodelName = submodel.get("keys")[0].get("value");

            // create regex pattern to find the important subshells
            Pattern pattern = Pattern.compile("Nameplate|CarbonFootprint|TechnicalData|HandoverDocumentation|ProductCondition|MaterialComposition|Circularity");
            Matcher matcher = pattern.matcher(SubmodelName);
            boolean matchFound = matcher.find();

            // TODO: if we find an important submodel fetch it and append it to the response
            if(matchFound) {
                String SubmodelIdentifyier = Base64.getEncoder().encodeToString(submodel);
                JsonNode SubResponse = restClient.get().uri(HostUrl + "/submodels/" + SubmodelIdentifyier);
            }

        }        
            return response.toString();
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