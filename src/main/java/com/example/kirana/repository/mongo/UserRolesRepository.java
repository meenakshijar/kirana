package com.example.kirana.repository.mongo;

import com.example.kirana.model.mongo.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRolesRepository extends MongoRepository<UserRole, String> {

    UserRole findByUserId(String userId);
}
