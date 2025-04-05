package com.jwtAuthentication.jwt.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Data
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @Column
    String username;
    @Column
    String password;
    @Column
    String email;
    @Column
    String firstName;
    @Column
    String lastName;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd") // Format for JSON responses
    private LocalDate dob;
    @Column
    private String country;
    @Column
    private String phoneNo;
    @Column
    private String bio;
    @Column
    private String image;
    @Column
    private String role;

}
