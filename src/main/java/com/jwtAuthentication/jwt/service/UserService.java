package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.DTO.requestDto.LoginRequest;
import com.jwtAuthentication.jwt.DTO.requestDto.UserRequestDto;
import com.jwtAuthentication.jwt.DTO.responseDto.LoginResponse;
import com.jwtAuthentication.jwt.controllers.AuthController;
import com.jwtAuthentication.jwt.model.User;
import com.jwtAuthentication.jwt.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Optional;

@Service
public class UserService {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public UserService(JwtService jwtService, PasswordEncoder passwordEncoder, UserRepository userRepository, AuthenticationManager authenticationManager) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
    }


    public LoginResponse verifyUser(LoginRequest loginRequest) {
        try {

            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            if (authentication.isAuthenticated()) {
                User user = userRepository.findByEmail(loginRequest.getEmail());
                String role = user.getRole();
                if ("user".equals(role)) {
                    String token = jwtService.generateToken(loginRequest.getEmail(), role);
                    return new LoginResponse(token, user, role);
                } else if ("admin".equals(role)) {
                    String token = jwtService.generateToken(loginRequest.getEmail(), role);
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

    public String deleteUser(int id) {
        Optional<User> user = userRepository.findById(id);
        if (user != null) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("User not found with id: " + id);
        }
        return "User deleted successfully " + id;
    }

    public User updateUser(User user,int id) {
        User existingUser = userRepository.findById(id).orElseThrow(
                () -> new RuntimeException("User not found"));

        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setDob(user.getDob());
        existingUser.setBio(user.getBio());
        existingUser.setImage(user.getImage());
        existingUser.setRole(user.getRole());

        // Only encode the password if it's being updated
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(existingUser);
    }

}

