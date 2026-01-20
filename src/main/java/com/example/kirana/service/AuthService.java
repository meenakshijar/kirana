package com.example.kirana.service;

import com.example.kirana.dao.UserDao;
import com.example.kirana.dao.UserRoleDao;
import com.example.kirana.dao.RoleDao;
import com.example.kirana.dto.LoginResponse;
import com.example.kirana.model.mongo.User;
import com.example.kirana.model.mongo.UserRole;
import com.example.kirana.model.mongo.Role;
import com.example.kirana.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    private final UserDao userDao;
    private final UserRoleDao userRoleDao;
    private final RoleDao roleDao;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserDao userDao,
                       UserRoleDao userRoleDao,
                       RoleDao roleDao,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userDao = userDao;
        this.userRoleDao = userRoleDao;
        this.roleDao = roleDao;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(String username, String password) {
        System.out.println("USERNAME RECEIVED = " + username);




        User user = userDao.findByUserName(username);

        if (user == null) {
            throw new RuntimeException("User not found");
        }
        System.out.println("DB HASH = " + user.getPassword());
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        UserRole userRole = userRoleDao.findByUserId(user.getUserId());
        if (userRole == null) {
            throw new RuntimeException("User has no role assigned");
        }

        Role role = roleDao.findById(userRole.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        String token = jwtUtil.generateToken(
                user.getUserName(),
                role.getRoleName(),
                userRole.getStoreId()
        );
        System.out.println("PASSWORD MATCH = " + passwordEncoder.matches(password,
                user.getPassword()));
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRole(role.getRoleName());          // ADMIN / USER / SUPER_ADMIN
        response.setStoreId(userRole.getStoreId());    // can be null for superadmin
        response.setExpiresIn(3600);

        return response;
    }

}
