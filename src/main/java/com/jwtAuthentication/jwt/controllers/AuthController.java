package com.jwtAuthentication.jwt.controllers;

import com.jwtAuthentication.jwt.DTO.requestDto.LoginRequest;
import com.jwtAuthentication.jwt.DTO.requestDto.SignUpRequestDto;
import com.jwtAuthentication.jwt.DTO.requestDto.UserRequestDto;
import com.jwtAuthentication.jwt.DTO.responseDto.LoginResponse;
import com.jwtAuthentication.jwt.mapper.UserMapper;
import com.jwtAuthentication.jwt.model.User;
import com.jwtAuthentication.jwt.service.DocumentService;
import com.jwtAuthentication.jwt.service.UserService;
import com.jwtAuthentication.jwt.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/auth")

public class AuthController {
    @Value("${file.upload-dir}")
    private String path;
    public static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerUser(
            @RequestBody SignUpRequestDto request) {

          userService.saveUser(request);

        ApiResponse<Void> response = new ApiResponse<>(
                true,
                "User registered successfully",
                null
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginUser(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = userService.verifyUser(loginRequest);
        ApiResponse<LoginResponse> response = new ApiResponse<>(
                true,
                "Login successful",
                loginResponse
        );

        return ResponseEntity.ok(response);
    }
}