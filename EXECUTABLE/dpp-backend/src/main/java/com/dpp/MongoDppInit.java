package com.dpp;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "dpp-repo")
public class MongoDppInit {
    @Id
    private String dppId;
    private String productId;
    private Instant createdAt;

    // 1.0.1
    private String version;

    private List<Submodels> submodels = new ArrayList<>();

       
    public String getDppID() {
        return dppId;
    }

    public void setDppID(String dppID) {
        this.dppId = dppID;
    }

    public String getProductID() {
        return productId;
    }

    public void setProductID(String productID) {
        this.productId = productID;
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
        private String reference;
        private String version;
        private String name;

        // Standard constructors
        public Submodels() {}

        public Submodels(String aasIdentifier, String name, String version) {
            this.reference = aasIdentifier;
            this.name = name;
            this.version = version;
        }

        // REQUIRED: Getters and Setters
        public String getReference() { return reference; }
        public void setReference(String aasIdentifier) { this.reference = aasIdentifier; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getVersion() {return this.version;}
        public void setVersion(String version) {this.version = version;}
    }
}
