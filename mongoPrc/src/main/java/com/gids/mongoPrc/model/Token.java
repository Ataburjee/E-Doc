package com.gids.mongoPrc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "activities")
@Data
@AllArgsConstructor
public class Token {

    @Id
    private String token;

    @JsonIgnore
    private String userId;

    @JsonIgnore
    private String password;

    private LocalDateTime loginTime;

    private LocalDateTime lastActivityTime;

    private LocalDateTime sessionExpiryTime;

    @JsonIgnore
    public boolean isActive() {
        return LocalDateTime.now().isBefore(this.sessionExpiryTime);
    }
}
