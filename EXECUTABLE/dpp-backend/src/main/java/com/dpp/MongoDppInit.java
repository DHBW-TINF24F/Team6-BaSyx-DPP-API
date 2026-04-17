package com.dpp;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "dpp-repo")
public class MongoDppInit {
    @Id
    private String dppID;
    private String productID;
    private Instant createdAt;

    // 1.0.1
    private String version;

    private List<Submodels> submodels = new ArrayList<>();

       
    public String getDppID() {
        return dppID;
    }

    public void setDppID(String dppID) {
        this.dppID = dppID;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Submodels> getSubmodels() {
        return submodels;
    }

    public void setSubmodels(List<Submodels> submodels) {
        this.submodels = submodels;
    }

    public static class Submodels {
        private String aasIdentifier;
        private String name;

        // Standard constructors
        public Submodels() {}

        public Submodels(String aasIdentifier, String name) {
            this.aasIdentifier = aasIdentifier;
            this.name = name;
        }

        // REQUIRED: Getters and Setters
        public String getAasIdentifier() { return aasIdentifier; }
        public void setAasIdentifier(String aasIdentifier) { this.aasIdentifier = aasIdentifier; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
