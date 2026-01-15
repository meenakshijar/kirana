package com.example.kirana.dao;

import com.example.kirana.model.mongo.Role;

import java.util.Optional;

public interface RoleDao {

    Optional<Role> findById(String roleId);

}
