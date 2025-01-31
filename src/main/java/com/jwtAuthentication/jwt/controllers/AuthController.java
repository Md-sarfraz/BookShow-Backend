package com.jwtAuthentication.jwt.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwtAuthentication.jwt.DTO.requestDto.LoginRequest;
import com.jwtAuthentication.jwt.DTO.responseDto.LoginResponse;
import com.jwtAuthentication.jwt.model.User;
import com.jwtAuthentication.jwt.service.DocumentService;
import com.jwtAuthentication.jwt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.Document;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentService documentService;

    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<?> registerUser(
            @RequestPart("user") String userJson,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.readValue(userJson, User.class);
            String responseMessage = userService.registerUser(user, profileImage);
            return ResponseEntity.ok(responseMessage);
        } catch (IOException e) {
            logger.error("Error while registering user: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error parsing user data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = userService.verifyUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login failed: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createDocument(
            @RequestPart("document") String documentJson,
            @RequestPart("file") MultipartFile file) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Document document = objectMapper.readValue(documentJson, Document.class);
            Document savedDocument = documentService.saveDocument(document, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);
        } catch (IOException e) {
            logger.error("Error while creating document: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error parsing document data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
}