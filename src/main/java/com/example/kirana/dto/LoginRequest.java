package com.example.kirana.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "userName is required")

    private String userName;

    @NotBlank(message = "password is required")

    private String password;

}

