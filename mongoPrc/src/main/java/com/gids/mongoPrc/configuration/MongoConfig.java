package com.gids.mongoPrc.configuration;

import com.mongodb.client.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MongoConfig {
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb://globalid:Bb%40cwcp4hKMI%23YCWcX%4039f0G@192.168.44.96:6000/?authMechanism=SCRAM-SHA-1&authSource=globalid");
    }
}

