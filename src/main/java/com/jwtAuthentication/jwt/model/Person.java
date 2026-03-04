package com.jwtAuthentication.jwt.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "role")
    private String role;
}
