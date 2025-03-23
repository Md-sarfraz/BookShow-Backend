package com.jwtAuthentication.jwt.mapper;

import com.jwtAuthentication.jwt.DTO.requestDto.UserRequestDto;
import com.jwtAuthentication.jwt.model.User;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Component
@ToString

public class UserMapper {
    public static UserRequestDto toDTO(User user) {
        UserRequestDto dto = new UserRequestDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setDOB(user.getDOB());
        dto.setBio(user.getBio());
        dto.setImage(user.getImage());
        dto.setRole(user.getRole());
        return dto;
    }

    public static User toEntity(UserRequestDto dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setDOB(dto.getDOB());
        user.setBio(dto.getBio());
        user.setImage(dto.getImage());
        user.setRole(dto.getRole());
        return user;
    }
}
