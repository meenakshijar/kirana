package com.example.kirana.dao;
import com.example.kirana.model.mongo.UserRole;

public interface UserRoleDao {
    UserRole findByUserId(String userId);
}
