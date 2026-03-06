package com.jwtAuthentication.jwt.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwtAuthentication.jwt.DTO.requestDto.EventRequestDto;
import com.jwtAuthentication.jwt.util.ApiResponse;
import com.jwtAuthentication.jwt.mapper.EventMapper;
import com.jwtAuthentication.jwt.model.Event;
import com.jwtAuthentication.jwt.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    @Autowired
    private ObjectMapper objectMapper;

    // Create Event
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Event>> addEvent(
            @RequestPart("event") String eventJson,
            @RequestPart("image") MultipartFile imageFile,
            @RequestPart("backgroundImage") MultipartFile backgroundImageFile) {

        try {
            EventRequestDto eventDto = objectMapper.readValue(eventJson, EventRequestDto.class);
            Event eventEntity = EventMapper.toEntity(eventDto);
            Event savedEvent = eventService.saveEvent(eventEntity, imageFile, backgroundImageFile);

            ApiResponse<Event> response = new ApiResponse<>(
                    true,
                    "Event created successfully",
                    savedEvent
            );

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();

            ApiResponse<Event> response = new ApiResponse<>(
                    false,
                    "Something went wrong: " + e.getMessage(),
                    null
            );

            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    // Get All Events
    @GetMapping("/findAll")
    public ResponseEntity<ApiResponse<List<EventRequestDto>>> getAllEvents() {
        List<EventRequestDto> events = eventService.getAllEvents();
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched all events successfully", events));
    }

    // Get Event By Id
    @GetMapping("/get/{id}")
    public ResponseEntity<ApiResponse<EventRequestDto>> getEventById(@PathVariable int id) {
        EventRequestDto event = eventService.getEventById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched event successfully", event));
    }
    // Update Event
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<String>> updateEvent(@PathVariable int id, @RequestBody EventRequestDto eventRequestDto) {
        String updatedEvent = eventService.updateEvent(id, eventRequestDto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Event updated successfully", updatedEvent));
    }

    // Delete Event
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteEvent(@PathVariable int id) {
        String deletedMessage = eventService.deleteEvent(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Event deleted successfully", deletedMessage));
    }

}
