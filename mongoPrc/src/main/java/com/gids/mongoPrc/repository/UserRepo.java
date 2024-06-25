package com.gids.mongoPrc.repository;

import com.gids.mongoPrc.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepo extends MongoRepository<User, String> {

}
