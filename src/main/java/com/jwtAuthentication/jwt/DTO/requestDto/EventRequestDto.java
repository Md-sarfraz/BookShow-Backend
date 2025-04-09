package com.jwtAuthentication.jwt.DTO.requestDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestDto {

    private String title;
    private String category;
    private String date;
    private String time;
    private String location;
    private String imageUrl;
    private String backgroundImageUrl;
    private String price;
    private String description;
}
