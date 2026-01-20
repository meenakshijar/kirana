package com.example.kirana.service;

import com.example.kirana.dto.CreateUserRequest;
import com.example.kirana.dto.CreateUserResponse;

public interface UserService {

    CreateUserResponse createUser(CreateUserRequest request);
}
