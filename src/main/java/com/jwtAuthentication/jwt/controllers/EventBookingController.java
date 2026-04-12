package com.jwtAuthentication.jwt.controllers;

import com.jwtAuthentication.jwt.DTO.requestDto.EventBookingRequestDto;
import com.jwtAuthentication.jwt.DTO.responseDto.EventBookingResponseDto;
import com.jwtAuthentication.jwt.DTO.responseDto.EventTicketDetailsResponseDto;
import com.jwtAuthentication.jwt.service.EventBookingService;
import com.jwtAuthentication.jwt.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event-bookings")
@RequiredArgsConstructor
public class EventBookingController {

    private final EventBookingService eventBookingService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<EventBookingResponseDto>> createBooking(@RequestBody EventBookingRequestDto request) {
        EventBookingResponseDto booking = eventBookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Event booking created successfully", booking));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<EventBookingResponseDto>>> getUserEventBookings(@PathVariable Long userId) {
        List<EventBookingResponseDto> bookings = eventBookingService.getBookingsByUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched user event bookings successfully", bookings));
    }

    @GetMapping("/ticket/{bookingReference}")
    public ResponseEntity<ApiResponse<EventTicketDetailsResponseDto>> getTicketByBookingReference(@PathVariable String bookingReference) {
        EventTicketDetailsResponseDto ticket = eventBookingService.getTicketByBookingReference(bookingReference);
        return ResponseEntity.ok(new ApiResponse<>(true, "Fetched event ticket successfully", ticket));
    }
}
