package com.gids.mongoPrc.controller;

import com.gids.mongoPrc.service.MongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConnectionController {

    private final MongoService mongoService;

    @Autowired
    public ConnectionController(MongoService mongoService) {
        this.mongoService = mongoService;
    }

    /*@PostMapping("/connection")
    public ResponseEntity<String> connect(@RequestParam String database) {

        if (mongoService.connect(database)) {
            return ResponseEntity.ok("Connected successfully to database: " + database);
        } else {
            return ResponseEntity.status(500).body("Failed to connect to database: " + database);
        }
    }*/

}
