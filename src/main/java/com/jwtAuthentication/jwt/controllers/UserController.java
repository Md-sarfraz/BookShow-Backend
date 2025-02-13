package com.jwtAuthentication.jwt.controllers;

import com.jwtAuthentication.jwt.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class UserController {
    public static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private UserService userService;

    @PostMapping("/post")
    public String postUser() {
        return "User created successfully";
    }

    @CrossOrigin("*")
    @PostMapping(value = "/public/uploadImg/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> registerUser(@RequestPart("image") MultipartFile file, @PathVariable int id) {
        try {

            String responseMessage = userService.uploadImg(id, file);
            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }


}
