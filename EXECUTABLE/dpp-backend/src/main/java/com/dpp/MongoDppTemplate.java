package com.dpp;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "dpp-repo")
public class MongoDppTemplate {
    @Id
    @Field("dppId")
    @JsonProperty("dppId")
    private String dppId;

    @JsonProperty("productId")
    private String productId;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("version")
    private String version;

    @JsonProperty("submodels")
    private List<Submodels> submodels = new ArrayList<>();

    // Corrected Getters and Setters to match field name "dppId" [cite: 211, 212]
    public String getDppId() {
        return dppId;
    }

    public void setDppId(String dppId) {
        this.dppId = dppId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
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

        public Submodels() {}

        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
    }
}