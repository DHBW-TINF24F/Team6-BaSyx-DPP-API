package com.dpp;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class DppBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DppBackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(MongoDBInterface mongoInterface) {
        return args -> {
            MongoDppInit a = new MongoDppInit();
            a.setDppID("test1");
            a.setProductID("prod-123");
            a.setCreatedAt(Instant.now()); // Set the timestamp

            mongoInterface.save(a);
            System.out.println("Successfully saved record to MongoDB!");
        };
    }
}

