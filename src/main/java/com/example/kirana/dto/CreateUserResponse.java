package com.example.kirana.dto;

import lombok.Data;

@Data
public class CreateUserResponse {

    private String userId;
    private String userName;
    private String role;
    private String storeId;
    private String message;
}
