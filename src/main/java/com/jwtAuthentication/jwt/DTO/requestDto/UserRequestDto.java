package com.jwtAuthentication.jwt.DTO.requestDto;

import lombok.Data;

@Data
public class UserRequestDto {

    private int id;
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private String DOB;
    private String Bio;
    private String image;
    private String role;

}
