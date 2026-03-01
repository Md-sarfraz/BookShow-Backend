package com.jwtAuthentication.jwt.controllers;

import com.jwtAuthentication.jwt.DTO.requestDto.UserRequestDto;
import com.jwtAuthentication.jwt.mapper.UserMapper;
import com.jwtAuthentication.jwt.model.User;
import com.jwtAuthentication.jwt.service.JwtService;
import com.jwtAuthentication.jwt.service.UserService;
import com.jwtAuthentication.jwt.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger logger =
            LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    /* ========================= GET USER BY ID ========================= */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/get/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable int id) {
        User user = userService.getUser(id);

        ApiResponse<User> response = new ApiResponse<>(
                true,
                "User fetched successfully",
                user
        );

        return ResponseEntity.ok(response);
    }

    /* ========================= GET USER ========================= */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get")
    public ResponseEntity<ApiResponse<User>> getUser(  @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        Integer id = jwtService.extractUserId(token);
        User user = userService.getUser(id);

        ApiResponse<User> response = new ApiResponse<>(
                true,
                "User fetched successfully",
                user
        );

        return ResponseEntity.ok(response);
    }

    /* ========================= DELETE USER ========================= */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable int id) {

        String message = userService.deleteUser(id);

        ApiResponse<String> response = new ApiResponse<>(
                true,
                "User deleted successfully",
                message
        );

        return ResponseEntity.ok(response);
    }

    /* ========================= UPDATE USER ========================= */
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PatchMapping("/update")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @RequestBody UserRequestDto userRequestDto,
            @RequestHeader("Authorization") String authHeader) {

        // Extract token from Authorization header
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        Integer userId = jwtService.extractUserId(token);

        User user = UserMapper.toEntity(userRequestDto);
        User updatedUser = userService.updateUser(user, userId);

        ApiResponse<User> response = new ApiResponse<>(
                true,
                "User updated successfully",
                updatedUser
        );

        return ResponseEntity.ok(response);
    }

    /* ========================= GET ALL USERS ========================= */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/findAll")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {

        List<User> users = userService.findAllUsers();

        ApiResponse<List<User>> response = new ApiResponse<>(
                true,
                users.isEmpty() ? "No users found" : "Users fetched successfully",
                users
        );

        return ResponseEntity.ok(response);
    }
}
