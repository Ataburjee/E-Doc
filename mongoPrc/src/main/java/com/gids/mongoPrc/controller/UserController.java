package com.gids.mongoPrc.controller;

import com.gids.mongoPrc.model.Credential;
import com.gids.mongoPrc.model.User;
import com.gids.mongoPrc.service.ActivityService;
import com.gids.mongoPrc.service.MongoService;
import com.gids.mongoPrc.service.UserService;
import com.gids.mongoPrc.utility.MongoUtility;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    public UserService userService;

    @Getter
    private final MongoService mongoService;

    @Autowired
    public UserController(MongoService mongoService) {
        this.mongoService = mongoService;
    }

    @Autowired
    public ActivityService activityService;

    @Autowired
    private MongoUtility utility;


    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) throws Exception {

        return userService.saveUser(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Credential credential) throws Exception {

        if (credential == null) {
            throw new RuntimeException();
        }
        return userService.login(credential);
    }

    @GetMapping({"", "/"})
    public ResponseEntity<?> getListUsers(@RequestHeader("Authorization") String token) throws Exception {

        ResponseEntity<?> response = utility.validateToken(token, "");
        if (response != null) {
            return response;
        }

        token = token.substring(7);
        return userService.listUsers(token);

    }

    @DeleteMapping("/expire")
    public ResponseEntity<?> deleteExpiredUsers() throws Exception {

        String message = activityService.removeExpiredSessions();

        return ResponseEntity.status(203).body(message);
    }

    @DeleteMapping("/")
    public ResponseEntity<?> deleteUsers() throws Exception {

        mongoService.getUsersCollection().drop();

        return ResponseEntity.status(203).body("Deleted successfully");
    }

}
