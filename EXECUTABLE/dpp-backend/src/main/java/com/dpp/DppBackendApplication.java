package com.dpp;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;


@SpringBootApplication
public class DppBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DppBackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(MongoDBInterface mongoInterface, MongoTemplate mongoTemplate) {
        return args -> {
            System.out.println("DB Name: " + mongoTemplate.getDb().getName());
            System.out.println("Collection exists before: " + mongoTemplate.collectionExists("dpp-repo"));

            if (!mongoTemplate.collectionExists("dpp-repo")) {
                mongoTemplate.createCollection("dpp-repo");
                System.out.println("Collection dpp-repo created manually.");
            }

            System.out.println("Collection exists after: " + mongoTemplate.collectionExists("dpp-repo"));
            System.out.println("Document count: " + mongoInterface.count());
            System.out.println("Successfully saved record to MongoDB!");
        };
    }
}

