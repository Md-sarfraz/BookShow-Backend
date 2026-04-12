package com.jwtAuthentication.jwt.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwtAuthentication.jwt.DTO.requestDto.EventRequestDto;
import com.jwtAuthentication.jwt.util.ApiResponse;
import com.jwtAuthentication.jwt.mapper.EventMapper;
import com.jwtAuthentication.jwt.model.Event;
import com.jwtAuthentication.jwt.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
        EventRequestDto eventDto;
        try {
            eventDto = objectMapper.readValue(eventJson, EventRequestDto.class);
        } catch (Exception ex) {
            throw new RuntimeException("Invalid event payload", ex);
        }

        Event eventEntity = EventMapper.toEntity(eventDto);
        Event savedEvent;
        try {
            savedEvent = eventService.saveEvent(eventEntity, imageFile, backgroundImageFile);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }

        ApiResponse<Event> response = new ApiResponse<>(
                true,
                "Event created successfully",
                savedEvent
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
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
    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<EventRequestDto>> updateEventWithMedia(
            @PathVariable int id,
            @RequestPart("event") String eventJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestPart(value = "backgroundImage", required = false) MultipartFile backgroundImageFile) {

        EventRequestDto eventRequestDto;
        try {
            eventRequestDto = objectMapper.readValue(eventJson, EventRequestDto.class);
        } catch (Exception ex) {
            throw new RuntimeException("Invalid event payload", ex);
        }

        EventRequestDto updatedEvent;
        try {
            updatedEvent = eventService.updateEvent(id, eventRequestDto, imageFile, backgroundImageFile);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }

        return ResponseEntity.ok(new ApiResponse<>(true, "Event updated successfully", updatedEvent));
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<EventRequestDto>> updateEvent(@PathVariable int id, @RequestBody EventRequestDto eventRequestDto) {
        EventRequestDto updatedEvent = eventService.updateEvent(id, eventRequestDto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Event updated successfully", updatedEvent));
    }

    // Delete Event
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteEvent(@PathVariable int id) {
        String deletedMessage = eventService.deleteEvent(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Event deleted successfully", deletedMessage));
    }

}
