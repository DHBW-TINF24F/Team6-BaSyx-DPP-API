package com.dpp.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import com.dpp.util.ValidateDPP;
import com.fasterxml.jackson.databind.JsonNode;
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

    /*
     * 
     * insert the provided dpp in to the mongoDB
     * 
     * 1. verify dpp structure
     * 2. connec to db
     * 3. insert in to db
     * 
     * JSON Format:
     * 
     *shell: {
     *          id : "<global asset id>",
     *          dpps: [
     *                  {
     *                      dppId : "<productId + timestamp>",
     *                      productId: "<aas Identifier>",
     *                      createdAt: time-stamp,
     *                      version : "<x.x.x>",
     *                      submodels: [
     *                                  {
     *                                      name: "CarbonFootPrint",
     *                                      version: "<x.x.x>"
     *                                  // can be NULL if not exists
     *                                      reference: "<submodel identifier>"
     *                                  },
     * *                                  {
     *                                      name: "DigitalNamePlate",
     *                                      version: "<x.x.x>"
     *                                      reference: "<submodel identifier>"
     *                                  },
     * *                                  {
     *                                      name: "TechnicalData",
     *                                      version: "<x.x.x>"
     *                                      reference: "<submodel identifier>"
     *                                  },
     * *                                  {
     *                                      name: "Condition",
     *                                      version: "<x.x.x>"
     *                                      reference: "<submodel identifier>"
     *                                  },
     * *                                  {
     *                                      name: "Composition",
     *                                      version: "<x.x.x>"
     *                                      reference: "<submodel identifier>"
     *                                  },
     * *                                  {
     *                                      name: "Circularity",
     *                                      version: "<x.x.x>"
     *                                      reference: "<submodel identifier>"
     *                                  },
     * *                                  {
     *                                      name: "HandOverDocumentation",
     *                                      version: "<x.x.x>"
     *                                      reference: "<submodel identifier>"
     *                                  },
     *                                  ]
     *                  }
     *                  ]
     * }
     * }
     * 
     */
    @PostMapping("/dpps")
    public ResponseEntity<ObjectNode> createDpp(@RequestBody JsonNode dpp) {
        ObjectNode response = mapper.createObjectNode();
        if (!ValidateDPP.validate(dpp)) {
            return ResponseEntity.badRequest().body(response.put("error", "invalid Dpp"));
        }
        

        response.put("status", "success");
        response.put("message", "DPP format is valid");
        return ResponseEntity.status(201).body(response);
    }

}
