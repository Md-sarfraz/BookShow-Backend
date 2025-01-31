package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.DTO.requestDto.LoginRequest;
import com.jwtAuthentication.jwt.DTO.responseDto.LoginResponse;
import com.jwtAuthentication.jwt.model.User;
import com.jwtAuthentication.jwt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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

    public String registerUser(User user, MultipartFile profileImage) {
        try {
            // Handle Profile Image
            if (profileImage != null && !profileImage.isEmpty()) {
                // Create the directory if it doesn't exist
                Files.createDirectories(Paths.get(uploadDir)); // Will create C:/myapp/uploads/
                String fileName = UUID.randomUUID().toString() + "_" + profileImage.getOriginalFilename();
                File file = new File(uploadDir + fileName);
                profileImage.transferTo(file); // Save the file
                user.setProfileImage(fileName);
            }

            // Save user to database (Assuming you have a repository)
            userRepository.save(user);
            return "User registered successfully!";
        } catch (IOException e) {
            return "Failed to upload profile image: " + e.getMessage();
        }
    }

    public LoginResponse verifyUser(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            if (authentication.isAuthenticated()) {
                User user = userRepository.findByEmail(loginRequest.getEmail());
                String token = jwtService.generateToken(loginRequest.getEmail());
                return new LoginResponse(token, user);
            }
        } catch (AuthenticationException e) {
            throw new RuntimeException("Failed to authenticate user");
        }
        return null;
    }
}
