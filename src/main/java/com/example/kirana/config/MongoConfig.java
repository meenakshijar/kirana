package com.example.kirana.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * The type Mongo config.
 */
@Configuration
public class MongoConfig {

    /**
     * Mongo client mongo client.
     *
     * @return the mongo client
     */
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb://localhost:27017");
    }

    /**
     * Mongo template mongo template.
     *
     * @return the mongo template
     */
    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), "kirana_db");
    }
}
