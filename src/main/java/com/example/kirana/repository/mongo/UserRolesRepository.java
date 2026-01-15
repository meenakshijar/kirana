package com.example.kirana.repository.mongo;

import com.example.kirana.model.mongo.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRolesRepository extends MongoRepository<UserRole,String>{


    Optional<UserRole> findByUserId(String userId);
}
