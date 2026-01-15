package com.example.kirana.repository.mongo;

import com.example.kirana.model.mongo.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RolesRepository extends MongoRepository<Role, String> {
    // nothing needed here
}
