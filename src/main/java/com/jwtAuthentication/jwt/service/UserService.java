package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.DTO.requestDto.LoginRequest;
import com.jwtAuthentication.jwt.DTO.responseDto.LoginResponse;
import com.jwtAuthentication.jwt.controllers.AuthController;
import com.jwtAuthentication.jwt.model.User;
import com.jwtAuthentication.jwt.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Value("${file.upload-dir}")
    private String uploadDir;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public String uploadImg(int id, MultipartFile image) {
        User user=userRepository.findById(id).orElseThrow(()-> new RuntimeException("No such user"));
        try {
            // Handle Profile Image
            if (image != null && !image.isEmpty()) {
                // Create the directory if it doesn't exist
                Files.createDirectories(Paths.get(uploadDir)); // Will create C:/myapp/uploads/
                String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                File file = new File(uploadDir + fileName);
                image.transferTo(file); // Save the file
                user.setImage(fileName);
            }
            // Save user to database (Assuming you have a repository)
            userRepository.save(user);
            return "Image uploaded successfully!";
        } catch (IOException e) {
            return "Failed to upload: " + e.getMessage();
        }
    }

    public LoginResponse verifyUser(LoginRequest loginRequest) {
        try {

            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            if (authentication.isAuthenticated()) {
                User user = userRepository.findByEmail(loginRequest.getEmail());
                String role=user.getRole();
                if ("user".equals(role)) {
                    String token = jwtService.generateToken(loginRequest.getEmail(),role);
                    return new LoginResponse(token, user, role);
                } else if ("admin".equals(role)) {
                    String token = jwtService.generateToken(loginRequest.getEmail(),role);
                    return new LoginResponse(token, user, role);
                } else {
                    throw new RuntimeException("Invalid role");
                }
            }
        } catch (AuthenticationException e) {
            throw new RuntimeException("Failed to authenticate user");
        }
        return null;
    }


    public InputStream getResources(String path, String fileName) throws FileNotFoundException {
        String fullPath = path + File.separator + fileName;

        return new FileInputStream(fullPath);
    }

    public User saveUser(User user) {
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("user");  // Default role
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
         return user;
    }

}
