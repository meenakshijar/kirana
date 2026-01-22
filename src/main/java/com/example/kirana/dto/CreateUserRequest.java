package com.example.kirana.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "userName is required")
    private String userName;
    @NotBlank(message = "password is required")
    private String password;
    @NotBlank(message = "role  is required")
    private String role;
    @NotBlank(message = "store id is required")// USER / ADMIN
    private String storeId;  // required for USER + ADMIN
}
