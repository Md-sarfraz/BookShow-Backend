package com.jwtAuthentication.jwt.DTO.responseDto;

import com.jwtAuthentication.jwt.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String role;
    private int id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String dob;
    private String country;
    private String phoneNo;
    private String bio;
    private String image;
}
