package com.gids.mongoPrc.service;

import com.gids.mongoPrc.model.Document;
import com.gids.mongoPrc.model.Role;
import com.gids.mongoPrc.model.ShareDocument;
import com.gids.mongoPrc.utility.MongoUtility;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
public class DocumentService {

    @Autowired
    private final MongoService mongoService;

    @Autowired
    private MongoUtility utility;

    @Autowired
    public DocumentService(MongoService mongoService) {
        this.mongoService = mongoService;
    }

    public MongoCollection<org.bson.Document> getDocumentCollection() {
        return mongoService.getDocumentsCollection();
    }

    public MongoCollection<org.bson.Document> getActivityCollection() {
        return mongoService.getActivityCollection();
    }

    public JSONObject createDocument(Document document) throws Exception {

        String owner = document.getOwner();

        FindIterable<org.bson.Document> userDoc = mongoService.getUsersCollection().find(new org.bson.Document("_id", owner));

        if (userDoc.first() == null) {
            return utility.getResponse("User not exists, please signup first");
        }

        ObjectId objectId = new ObjectId();
        String id = objectId.toString();

        org.bson.Document doc = new org.bson.Document("_id", id)
                .append("title", document.getTitle())
                .append("content", document.getContent())
                .append("owner", owner)
                .append("collaborators", new LinkedHashSet<>());

        mongoService.getDocumentsCollection().insertOne(doc);

        return utility.getResponse("Document created successfully");

    }

    public JSONObject listDocumentsOfUser(String userId) throws Exception {

        MongoCursor<org.bson.Document> iterator = getDocumentCollection().find(new org.bson.Document("owner", userId)).iterator();

        if (!iterator.hasNext()) {
            return utility.getResponse("No User exists");
        }

        JSONArray jsonArray = new JSONArray();

        while (iterator.hasNext()) {
            org.bson.Document document = iterator.next();
            JSONObject jsonObject = new JSONObject(document);
            jsonArray.add(jsonObject);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("documents", jsonArray);

        return jsonObject;

    }

    public JSONObject getDocumentOfUser(String userId, String docId) {

        org.bson.Document filterDoc = new org.bson.Document("owner", userId).append("_id", docId);

        MongoCursor<org.bson.Document> iterator = getDocumentCollection().find(filterDoc).iterator();

        if (!iterator.hasNext()) {
            return null;
        }

        JSONObject docObj = new JSONObject(iterator.next());

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("document", docObj);

        return jsonObject;
    }

    public ResponseEntity<?> shareDocument(ShareDocument requestBody) {

        String documentId = requestBody.getDocumentId();

        org.bson.Document filterDoc = new org.bson.Document("_id", documentId);

        MongoCursor<org.bson.Document> iterator = getDocumentCollection().find(filterDoc).iterator();

        if (!iterator.hasNext()) {
            return ResponseEntity.status(404).body(utility.getResponse("Document not found"));
        }

        org.bson.Document document = iterator.next();

        if (!document.getString("owner").equals(requestBody.getOwner())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(utility.getResponse("User not authorized"));
        }

        Map<String, List<String>> collaborators = null;

        if (filterDoc.get("collaborators") == null) {
            collaborators = new HashMap<>();
        } else {
            collaborators = (Map<String, List<String>>) filterDoc.get("collaborators");
        }

        String recipientEmail = requestBody.getRecipientEmail();

        List<String> accessType = requestBody.getAccessType();

        for (String access : accessType) {
            if (!isValidAccessType(access)) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(utility.getResponse("User not authorized"));
            }
        }

        if (collaborators.containsKey(recipientEmail)) {

            List<String> existingAccess = collaborators.get(recipientEmail);

            for (String access : accessType) {
                if (existingAccess.contains(access)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(utility.getResponse("User already has the access"));
                } else {
                    existingAccess.add(access);
                }
            }
            collaborators.put(recipientEmail, existingAccess);

        } else {
            collaborators.put(recipientEmail, accessType);
        }

        org.bson.Document updateDoc = new org.bson.Document("$set", new org.bson.Document("collaborators", collaborators));

        UpdateResult result = getDocumentCollection().updateOne(filterDoc, updateDoc);

        if (result.wasAcknowledged()) {
            return ResponseEntity.status(200).body(utility.getResponse("Document shared successfully"));
        } else {
            return ResponseEntity.status(500).body(utility.getResponse("Failed to share the document"));
        }
    }

    private boolean isValidAccessType(String accessType) {

        for (Role type : Role.values()) {
            if (type.name().equalsIgnoreCase(accessType)) {
                return true;
            }
        }
        return false;
    }

}
