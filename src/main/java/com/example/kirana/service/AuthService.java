package com.example.kirana.service;

import com.example.kirana.dao.UserDao;
import com.example.kirana.dao.UserRoleDao;
import com.example.kirana.dao.RoleDao;
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

    public String login(String username, String password) {

        // 1️⃣ Fetch user
        User user = userDao.findByUserName(username);

        // 2️⃣ Validate password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // 3️⃣ Fetch user-role mapping (ONE role)
        UserRole userRole = userRoleDao.findByUserId(user.getUserId());
        if (userRole == null) {
            throw new RuntimeException("User has no role assigned");
        }

        // 4️⃣ Fetch role
        Role role = roleDao.findById(userRole.getRoleId())
                .orElseThrow(() ->
                        new RuntimeException("Role not found")
                );

        // 5️⃣ Generate JWT
        return jwtUtil.generateToken(
                user.getUserName(),
                role.getRoleName(),
                userRole.getStoreId()
        );
    }
}
