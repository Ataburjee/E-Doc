package com.gids.mongoPrc.service;

import com.gids.mongoPrc.model.Credential;
import com.gids.mongoPrc.model.Token;
import com.gids.mongoPrc.model.User;
import com.gids.mongoPrc.utility.MongoUtility;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private final MongoService mongoService;

    @Autowired
    public UserService(MongoService mongoService) {
        this.mongoService = mongoService;
    }

    @Autowired
    ActivityService activityService;

    @Autowired
    private MongoUtility utility;

    public MongoCollection<Document> getUsersCollection() {
        return mongoService.getUsersCollection();
    }


    public ResponseEntity<?> listUsers(String tokenId) throws Exception {

        MongoCursor<Document> iterator = getUsersCollection().find().iterator();

        if (!iterator.hasNext()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new JSONObject());
        }

        JSONArray jsonArray = new JSONArray();
        while (iterator.hasNext()) {

            org.bson.Document document = iterator.next();

            JSONObject jsonObject = new JSONObject(getFormattedDoc(document));

            jsonArray.add(jsonObject);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Users", jsonArray);

        System.out.println("responseBody = " + jsonObject.toString());

        return ResponseEntity.status(HttpStatus.OK).body(jsonObject);
    }

    private Document getFormattedDoc(Document document) {
        Document doc = (Document) document.get("activity");
        String token = doc.getString("token");
        doc.remove("token");
        doc.remove("userId");
        doc.remove("password");
        document.replace("activity", doc);
        document.append("token", token);
        return document;
    }

    public ResponseEntity<?> saveUser(User user) throws Exception {

        if (ifUserExists(user)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(utility.getResponse("Email already exists!"));
        }

        ObjectId objectId = new ObjectId();
        String id = objectId.toString();

        Document document = new Document("_id", id)
                .append("name", user.getName())
                .append("email", user.getEmail())
                .append("credential", new Document("username", user.getEmail())
                        .append("password", user.getCredential().getPassword()));

        getUsersCollection().insertOne(document);

        return ResponseEntity.status(HttpStatus.CREATED).body(utility.getResponse("Signup successfully, id = " + id));
    }


    public ResponseEntity<?> login(Credential credential) throws Exception {

        String userId = credential.getUserId();

        MongoCursor<Document> userItr = getUsersCollection().find(new Document("email", userId)).iterator();

        if (!userItr.hasNext()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(utility.getResponse("Please provide a valid user id"));
        }

        User user = docToUserClass(userItr.next());
        Credential credentialObj = user.getCredential();

        String username = credential.getUserId();
        String password = credential.getPassword();

        if (credentialObj != null) {

            if (!credentialObj.getUserId().equals(username)
                    || !credentialObj.getPassword().equals(password)) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(utility.getResponse("Incorrect username or password"));
            }
        }

        String token = activityService.save(user);
        System.out.println("token = " + token);

        JSONObject response = utility.getResponse("Login successful");
        response.put("user", getUser(userId));

        return ResponseEntity.ok(response);
    }

    private JSONObject getUser(String userEmail) {

        MongoCursor<Document> userItr = getUsersCollection().find(new Document("email", userEmail)).iterator();

        if (!userItr.hasNext()) {
            return null;
        }
        JSONObject jsonObject = new JSONObject(getFormattedDoc(userItr.next()));

        return jsonObject;
    }

    //User login
    public boolean ifUserExists(User user) {

        for (Document doc : getUsersCollection().find()) {
            if (doc.getString("email").equals(user.getEmail())) {
                return true;
            }
        }
        return false;
    }

    public User docToUserClass(Document document) {

        String id = String.valueOf(document.get("_id"));
        String name = document.getString("name");
        String email = document.getString("email");
        Document credentialDoc = (Document) document.get("credential");

        Token token = null;
        boolean isActive = false;

        if (document.get("isActive") != null) {
            isActive = document.getBoolean("isActive");
        }
        if (isActive) {
            token = activityService.docToActiveUserClass((Document) document.get("activity"));
        }

        Credential credential = new Credential(email, credentialDoc.getString("password"));

        return new User(id, name, email, credential, isActive, token);
    }

}
