package com.jwtAuthentication.jwt.mapper;

import com.jwtAuthentication.jwt.DTO.requestDto.EventRequestDto;
import com.jwtAuthentication.jwt.model.Event;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Component
@ToString
public class EventMapper {

    public static EventRequestDto toDTO(Event event) {
        EventRequestDto dto = new EventRequestDto();
        dto.setTitle(event.getTitle());
        dto.setCategory(event.getCategory());
        dto.setDate(event.getDate());
        dto.setLocation(event.getLocation());
        dto.setImageUrl(event.getImageUrl());
        dto.setPrice(event.getPrice());
        return dto;
    }

    public static Event toEntity(EventRequestDto dto) {
        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setCategory(dto.getCategory());
        event.setDate(dto.getDate());
        event.setLocation(dto.getLocation());
        event.setImageUrl(dto.getImageUrl());
        event.setPrice(dto.getPrice());
        return event;
    }
}
