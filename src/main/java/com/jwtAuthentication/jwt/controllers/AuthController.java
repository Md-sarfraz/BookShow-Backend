package com.jwtAuthentication.jwt.controllers;

import com.jwtAuthentication.jwt.DTO.requestDto.LoginRequest;
import com.jwtAuthentication.jwt.DTO.responseDto.LoginResponse;
import com.jwtAuthentication.jwt.model.User;
import com.jwtAuthentication.jwt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")

public class AuthController {
    @Autowired
    private UserService userService;
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(
            @RequestPart("user") User user,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        System.out.println("Registering");
        return ResponseEntity.ok( userService.registerUser(user,profileImage));
    }
    @PostMapping("/login")
    public LoginResponse loginUser(@RequestBody LoginRequest loginRequest) {
      return userService.verifyUser(loginRequest);
}
}
