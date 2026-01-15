package com.example.kirana.dto;


import lombok.Data;
import lombok.Getter;
@Data
@Getter
public class LoginRequest {
    private String userName;
    private String password;

}

