package com.example.kirana.dto;

import lombok.Data;

@Data
public class CreateUserRequest {

    private String userName;
    private String password;
    private String role;     // USER / ADMIN
    private String storeId;  // required for USER + ADMIN
}
