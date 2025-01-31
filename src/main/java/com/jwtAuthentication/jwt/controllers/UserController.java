package com.jwtAuthentication.jwt.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @PostMapping("/post")
    public String postUser() {
        return "User created successfully";
    }
}
