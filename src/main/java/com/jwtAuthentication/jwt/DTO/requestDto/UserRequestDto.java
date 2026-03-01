package com.jwtAuthentication.jwt.DTO.requestDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jwtAuthentication.jwt.model.Role;
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
    
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dob;
    
    private String country;
    private String phoneNo;
    private String bio;
    private String image;
    private Role role;

}
