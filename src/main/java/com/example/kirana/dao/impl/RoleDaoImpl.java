package com.example.kirana.dao.impl;

import com.example.kirana.dao.RoleDao;
import com.example.kirana.model.mongo.Role;
import com.example.kirana.repository.mongo.RolesRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public class RoleDaoImpl implements RoleDao {

    private final RolesRepository roleRepository;

    public RoleDaoImpl(RolesRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<Role> findById(String roleId) {
        return roleRepository.findById(roleId);
    }

}

