package com.example.kirana.repository.mongo;

import com.example.kirana.model.mongo.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RolesRepository extends MongoRepository<Role, String> {
    Optional<Role> findByRoleId(String roleId);
    Optional<Role> findByRoleName(String roleName);
}
