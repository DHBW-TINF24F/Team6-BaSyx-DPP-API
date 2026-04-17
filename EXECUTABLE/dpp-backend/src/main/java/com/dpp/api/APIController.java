package com.dpp.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping
public class APIController {


     private static final Logger logger = LoggerFactory.getLogger(APIController.class);

    private final ObjectMapper mapper;
    private final RestClient restClient = RestClient.create();
    private final WebClient webClient = WebClient.builder().build();

    public APIController(ObjectMapper mapper) {
        this.mapper = mapper;
    }




     @GetMapping("/health")
    public ResponseEntity<ObjectNode> health() {
        ObjectNode node = mapper.createObjectNode();
        node.put("status", "UP");
        return ResponseEntity.ok(node);
    }


    
}
