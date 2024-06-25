package com.gids.mongoPrc.model;


import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.Map;

@Data
@org.springframework.data.mongodb.core.mapping.Document(collection = "documents")
public class Document {

    @Id
    private String id;

    private String title;

    private String content;

    //Owner email
    private String owner;

    //Map<userId, List<accessType>>
    private Map<String, List<String>> collaborators;

    public Document(String id, String title, String content, String owner) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.owner = owner;
    }
}
