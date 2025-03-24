package com.jwtAuthentication.jwt.DTO.requestDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRequestDto {

    private int id;
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDate dob;
    private String bio;
    private String image;
    private String role;

}
