package com.gids.mongoPrc.controller;

import com.gids.mongoPrc.model.Document;
import com.gids.mongoPrc.model.ShareDocument;
import com.gids.mongoPrc.service.DocumentService;
import com.gids.mongoPrc.utility.MongoUtility;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private MongoUtility utility;

    //Create a document
    @PostMapping("/add")
    public ResponseEntity<?> addDocument(@RequestHeader("Authorization") String token, @RequestBody Document document) throws Exception {

        ResponseEntity<?> response = utility.validateToken(token, document.getOwner());

        if (response != null) return response;

        JSONObject message = documentService.createDocument(document);
        return ResponseEntity.status(200).body(message);
    }

    //Get all the documents for a particular user
    @GetMapping("documents")
    public ResponseEntity<?> getDocuments(@RequestHeader("Authorization") String token, @RequestParam("id") String userId) throws Exception {

        ResponseEntity<?> response = utility.validateToken(token, userId);

        if (response != null) return response;

        JSONObject message = documentService.listDocumentsOfUser(userId);

        return ResponseEntity.status(200).body(message);
    }

    //Get a particular document for an user
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocumentOfAnUser(@RequestHeader("Authorization") String token, @RequestParam("userId") String userId, @PathVariable String id) throws Exception {

        ResponseEntity<?> response = utility.validateToken(token, userId);

        if (response != null) return response;

        JSONObject message = documentService.getDocumentOfUser(userId, id);

        return ResponseEntity.status(200).body(message);
    }


    //Share document with a person
    @PatchMapping("/{id}/share")
    public ResponseEntity<?> shareDocument(@RequestBody ShareDocument document) throws Exception {

        return documentService.shareDocument(document);
    }

}
