package com.gids.mongoPrc.utility;

import com.gids.mongoPrc.service.MongoService;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class MongoUtility {

    @Autowired
    private final MongoService mongoService;

    @Autowired
    public MongoUtility(MongoService mongoService) {
        this.mongoService = mongoService;
    }

    public MongoCollection<org.bson.Document> getActivityCollection() {
        return mongoService.getActivityCollection();
    }

    public ResponseEntity<?> validateToken(String token, String userId) {

        JSONObject jsonObject = new JSONObject();

        if (token == null || !token.startsWith("Bearer ")) {
            jsonObject.put("message", "Please provide bearer token!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
        }

        token = token.substring(7);
        MongoCursor<Document> findToken = getActivityCollection().find(new Document("activity.token", token)).iterator();

        if (!findToken.hasNext()) {
            jsonObject.put("message", "Not a valid token, please login first!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(jsonObject);
        }

        /*if (!userId.isEmpty() && !validateUserByToken(token, userId)) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(getResponse("message", "User has no access"));
        }*/

        if (userId != null && !userId.isEmpty()) {
            Document doc = findToken.next();
            if (doc.get("userId") != null) {
                if (doc.getString("userId").equals(userId)) {
                    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(getResponse("User has no access"));
                }
            }
        }
        return null;
    }

    public boolean validateUserByToken(String token, String userId) {
        if (token.startsWith(userId)) {
            MongoCursor<Document> findToken = getActivityCollection().find(new Document("userId", userId)).iterator();
            if (findToken.hasNext()) {
                Document doc = findToken.next();
                if (doc.get("token") != null) {
                    if (doc.getString("token").equals(token)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public JSONObject getResponse(String value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", value);
        return jsonObject;
    }

}
