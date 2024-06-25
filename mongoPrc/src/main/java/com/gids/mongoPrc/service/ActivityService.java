package com.gids.mongoPrc.service;

import com.gids.mongoPrc.model.Token;
import com.gids.mongoPrc.model.User;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class ActivityService {

    @Autowired
    public MongoService mongoService;

    public Set<Token> tokenList = new HashSet<>();

    @Autowired
    public ActivityService(MongoService mongoService) {
        this.mongoService = mongoService;
    }

    public MongoCollection<Document> getCollection() {
        return mongoService.getUsersCollection();
    }

    public MongoCollection<Document> getActivityCollection() {
        return mongoService.getActivityCollection();
    }

    public String save(User user) {
        try {
            LocalDateTime ct = LocalDateTime.now();
            String accessToken = UUID.randomUUID().toString();

            String formattedAccessToken = user.getEmail() + accessToken + user.getCredential().getPassword();

            Token token = new Token(formattedAccessToken, user.getEmail(), user.getCredential().getPassword(), ct, ct, ct.plusDays(1));

            getActivityCollection().insertOne(tokenToDoc(token));

            user.setActivity(token);
            user.setActive(true);

            getCollection().replaceOne(new Document("email", user.getEmail()), userToDoc(user));

            return accessToken;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Document userToDoc(User user) {

        Token activity = user.getActivity();

        Document tokenDoc = null;

        if (activity != null) {
            tokenDoc = tokenToDoc(activity);
        }

        Document credDoc = new Document("userId", user.getEmail())
                .append("password", user.getCredential().getPassword());

        return new Document("_id", user.getId())
                .append("name", user.getName())
                .append("email", user.getEmail())
                .append("credential", credDoc)
                .append("isActive", user.isActive())
                .append("activity", tokenDoc);
    }

    private Document tokenToDoc(Token activity) {
        return new Document("token", activity.getToken())
                .append("userId", activity.getUserId())
                .append("loginTime", activity.getLoginTime())
                .append("lastActivityTime", activity.getLastActivityTime())
                .append("sessionExpiryTime", activity.getSessionExpiryTime());
    }

    public ResponseEntity<List<Document>> getActiveUsers() {
        MongoCursor<Document> iterator = getCollection().find().iterator();

        if (!iterator.hasNext()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        List<Document> documents = new ArrayList<>();

        while (iterator.hasNext()) {
            documents.add(iterator.next());
        }
        return ResponseEntity.status(HttpStatus.OK).body(documents);
    }

    public void removeUser(String sessionId) {
        getCollection().findOneAndDelete(new Document("sessionId", sessionId));
    }

    public Token getUserBySessionId(String token) {
        FindIterable<Document> documents = getCollection().find(new Document("token", token));
        if (documents.iterator().hasNext()) {
            return docToActiveUserClass(documents.iterator().next());
        } else {
            return null;
        }
    }

    public boolean isUserActive(String token) {
        return getUserBySessionId(token).isActive();
    }

    public Token docToActiveUserClass(Document document) {

        String id = document.getString("userId");
        String password = document.getString("password");
        String token = document.getString("token");
        LocalDateTime loginTime = dateToLocalDateTime(document.getDate("loginTime"));
        LocalDateTime lastActivityTime = dateToLocalDateTime(document.getDate("lastActivityTime"));
        LocalDateTime sessionExpiryTime = dateToLocalDateTime(document.getDate("sessionExpiryTime"));

        return new Token(token, id, password, loginTime, lastActivityTime, sessionExpiryTime);
    }

    public LocalDateTime dateToLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public void updateLastActivity(String sessionId) {

        Document filterDoc = new Document("sessionId", sessionId);
        Document updateDoc = new Document("$set", new Document("lastActivityTime", LocalDateTime.now()));

        getCollection().updateOne(filterDoc, updateDoc);
    }

    public String removeExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();

        Bson filter = Filters.lt("sessionExpiryTime", now);

        DeleteResult deleteResult = getCollection().deleteMany(filter);

        System.out.println("deleteResult.getDeletedCount() = " + deleteResult.getDeletedCount());

        return "deleted successfully, total deleted = " + deleteResult.getDeletedCount();

    }


}
