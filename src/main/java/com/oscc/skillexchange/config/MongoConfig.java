package com.oscc.skillexchange.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import java.util.ArrayList;

/**
 * Mongo-DB configuration with auditing and index management
 */
@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.oscc.skillexchange.repository")
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(new ArrayList<>());
    }

}
