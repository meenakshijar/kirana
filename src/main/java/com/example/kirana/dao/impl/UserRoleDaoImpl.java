package com.example.kirana.dao.impl;

import com.example.kirana.dao.UserRoleDao;
import com.example.kirana.model.mongo.UserRole;
import com.example.kirana.repository.mongo.UserRolesRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserRoleDaoImpl implements UserRoleDao {

    private final UserRolesRepository userRoleRepository;

    public UserRoleDaoImpl(UserRolesRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public UserRole findByUserId(String userId) {
        UserRole userRole = userRoleRepository.findByUserId(userId);

        if (userRole == null) {
            throw new RuntimeException("User role mapping not found for userId: " + userId);
        }

        return userRole;
    }
}
