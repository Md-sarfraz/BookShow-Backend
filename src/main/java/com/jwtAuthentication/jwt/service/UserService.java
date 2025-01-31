package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.DTO.requestDto.LoginRequest;
import com.jwtAuthentication.jwt.DTO.responseDto.LoginResponse;
import com.jwtAuthentication.jwt.model.User;
import com.jwtAuthentication.jwt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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

    public String registerUser(User user, MultipartFile profileImage) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Handle profile image upload
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                String uploadDir = "uploads/";
                File uploadPath = new File(uploadDir);
                if (!uploadPath.exists()) {
                    uploadPath.mkdirs();  // Create directory if it does not exist
                }

                String uniqueFileName = UUID.randomUUID().toString() + "_" + profileImage.getOriginalFilename();
                String filePath = uploadDir + uniqueFileName;
                profileImage.transferTo(new File(filePath));

                user.setProfileImage(filePath); // Save image path to database
            } catch (IOException e) {
                return "Failed to upload profile image";
            }
        }

        userRepository.save(user);
        return "User registered successfully";
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
