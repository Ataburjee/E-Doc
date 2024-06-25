package com.gids.mongoPrc.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Credential {

    private String userId;
    private String password;

}
