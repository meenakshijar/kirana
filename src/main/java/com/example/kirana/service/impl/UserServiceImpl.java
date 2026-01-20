package com.example.kirana.service.impl;

import com.example.kirana.dto.CreateUserRequest;
import com.example.kirana.dto.CreateUserResponse;
import com.example.kirana.model.mongo.Role;
import com.example.kirana.model.mongo.User;
import com.example.kirana.model.mongo.UserRole;
import com.example.kirana.repository.mongo.RolesRepository;
import com.example.kirana.repository.mongo.UserRepository;
import com.example.kirana.repository.mongo.UserRolesRepository;
import com.example.kirana.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RolesRepository roleRepository;
    private final UserRolesRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RolesRepository roleRepository,
                           UserRolesRepository userRoleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public CreateUserResponse createUser(CreateUserRequest request) {

        if (request.getUserName() == null || request.getUserName().isBlank()) {
            throw new RuntimeException("userName is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("password is required");
        }

        if (request.getRole() == null || request.getRole().isBlank()) {
            throw new RuntimeException("role is required");
        }

        String roleName = request.getRole().toUpperCase();

        if (!roleName.equals("USER") && !roleName.equals("ADMIN")) {
            throw new RuntimeException("Only USER or ADMIN can be created from this API");
        }

        // ✅ FIXED duplicate check
        Optional<User> existing = userRepository.findByUserName(request.getUserName());
        if (existing.isPresent()) {
            throw new RuntimeException("Username already exists: " + request.getUserName());
        }

        if (request.getStoreId() == null || request.getStoreId().isBlank()) {
            throw new RuntimeException("storeId is required for USER/ADMIN");
        }

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found in DB: " + roleName));

        String userId = "USR_" + UUID.randomUUID();

        User user = new User();
        user.setUserId(userId);
        user.setUserName(request.getUserName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        // ✅ FIXED userRoleId set
        UserRole userRole = new UserRole();
        userRole.setUserRoleId("UR_" + UUID.randomUUID());
        userRole.setUserId(userId);
        userRole.setRoleId(role.getRoleId());
        userRole.setStoreId(request.getStoreId());

        userRoleRepository.save(userRole);

        CreateUserResponse response = new CreateUserResponse();
        response.setUserId(userId);
        response.setUserName(user.getUserName());
        response.setRole(roleName);
        response.setStoreId(request.getStoreId());
        response.setMessage("User created successfully");

        return response;
    }
}
