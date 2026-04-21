package com.dpp;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "aas-env";
    }

    @Override
    public MongoClient mongoClient() {
        String mongoUri = System.getenv("SPRING_DATA_MONGODB_URI");
        if (mongoUri == null || mongoUri.isEmpty()) {
            mongoUri = "mongodb://mongoAdmin:mongoPassword@127.0.0.1:27017/aas-env?authSource=admin";
        }

        ConnectionString connectionString = new ConnectionString(mongoUri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(settings);
    }
}