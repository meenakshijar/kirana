package com.example.kirana.controller;

import com.example.kirana.dto.LoginRequest;
import com.example.kirana.dto.LoginResponse;
import com.example.kirana.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * The type Auth controller.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Instantiates a new Auth controller.
     *
     * @param authService the auth service
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Login response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {


        LoginResponse response = authService.login(
                request.getUserName(),
                request.getPassword()
        );

        return ResponseEntity.ok(response);
    }
}
