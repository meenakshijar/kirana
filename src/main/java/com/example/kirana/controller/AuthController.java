package com.example.kirana.controller;

import com.example.kirana.dto.LoginRequest;
import com.example.kirana.dto.LoginResponse;
import com.example.kirana.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {


        LoginResponse response = authService.login(
                request.getUserName(),
                request.getPassword()
        );

        return ResponseEntity.ok(response);
    }
}
