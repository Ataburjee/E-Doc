package com.gids.mongoPrc.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MongoService {

    private final MongoClient mongoClient;
    private boolean isNotConnected;

    @Value("${mongo.database.name}")
    private String databaseName;

    @Getter
    private MongoCollection<Document> usersCollection;

    @Getter
    private MongoCollection<Document> documentsCollection;

    @Getter
    private MongoCollection<Document> activityCollection;

    @Autowired
    public MongoService(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        this.isNotConnected = true;
    }
    @PostConstruct
    public void connect() {
        try {
            usersCollection = mongoClient.getDatabase(databaseName).getCollection("users");
            documentsCollection = mongoClient.getDatabase(databaseName).getCollection("documents");
            activityCollection = mongoClient.getDatabase(databaseName).getCollection("activities");
            isNotConnected = false;
        } catch (Exception e) {
            isNotConnected = true;
        }
    }

    public boolean isNotConnected() {
        return isNotConnected;
    }

}

