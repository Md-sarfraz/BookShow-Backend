package com.jwtAuthentication.jwt.controllers;

import com.jwtAuthentication.jwt.DTO.requestDto.UserRequestDto;
import com.jwtAuthentication.jwt.mapper.UserMapper;
import com.jwtAuthentication.jwt.model.User;
import com.jwtAuthentication.jwt.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    public static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private UserService userService;
    @GetMapping("/get/{id}")
    public ResponseEntity<?>getUser(@PathVariable int id){
        return userService.getUser(id);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable int id){
        String response = userService.deleteUser(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PutMapping("/update/{id}")
    public ResponseEntity<?> editUser(@RequestBody UserRequestDto userRequestDto,@PathVariable int id){
        User user= UserMapper.toEntity(userRequestDto);
        User updatedUser = userService.updateUser(user,id);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAllUsers();
        if (users.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.ok(users); // 200 OK
    }
}


