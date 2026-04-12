package com.jwtAuthentication.jwt.controllers;

import com.jwtAuthentication.jwt.DTO.requestDto.EventAvailabilityRequestDto;
import com.jwtAuthentication.jwt.DTO.requestDto.EventConfirmBookingRequestDto;
import com.jwtAuthentication.jwt.DTO.requestDto.EventLockTicketsRequestDto;
import com.jwtAuthentication.jwt.DTO.requestDto.EventPaymentOrderRequestDto;
import com.jwtAuthentication.jwt.DTO.requestDto.EventReleaseLockRequestDto;
import com.jwtAuthentication.jwt.DTO.responseDto.EventAvailabilityResponseDto;
import com.jwtAuthentication.jwt.DTO.responseDto.EventConfirmBookingResponseDto;
import com.jwtAuthentication.jwt.DTO.responseDto.EventLockTicketsResponseDto;
import com.jwtAuthentication.jwt.DTO.responseDto.EventPaymentOrderResponseDto;
import com.jwtAuthentication.jwt.service.EventBookingService;
import com.jwtAuthentication.jwt.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequiredArgsConstructor
public class EventBookingFlowController {

    private final EventBookingService eventBookingService;

        @PostMapping("/events/check-availability")
    public ResponseEntity<ApiResponse<EventAvailabilityResponseDto>> checkAvailability(
            @RequestBody EventAvailabilityRequestDto request
    ) {
        EventAvailabilityResponseDto response = eventBookingService.checkAvailability(
                request.getEventId(),
                request.getTicketCount()
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Availability checked", response));
    }

        @PostMapping("/events/lock-tickets")
    public ResponseEntity<ApiResponse<EventLockTicketsResponseDto>> lockTickets(
            @RequestBody EventLockTicketsRequestDto request
    ) {
        EventLockTicketsResponseDto response = eventBookingService.lockTickets(
                request.getEventId(),
                request.getUserId(),
                request.getTicketCount()
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Tickets locked", response));
    }

    @PostMapping("/event-payment/create-order")
    public ResponseEntity<ApiResponse<EventPaymentOrderResponseDto>> createOrder(
            @RequestBody EventPaymentOrderRequestDto request
    ) {
        try {
            EventPaymentOrderResponseDto response = eventBookingService.createPaymentOrder(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment order created", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/event-payment/verify")
    public ResponseEntity<ApiResponse<EventConfirmBookingResponseDto>> confirmBooking(
            @RequestBody EventConfirmBookingRequestDto request
    ) {
        try {
            EventConfirmBookingResponseDto response = eventBookingService.confirmBooking(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Payment verified and booking confirmed", response));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(new ApiResponse<>(false, "Payment verification failed. Please contact support.", null));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Payment verification error: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/event-payment/failed")
    public ResponseEntity<ApiResponse<Void>> handlePaymentFailed(@RequestParam String razorpayOrderId) {
        if (razorpayOrderId == null || razorpayOrderId.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "razorpayOrderId is required", null));
        }

        eventBookingService.handlePaymentFailed(razorpayOrderId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Booking marked as failed", null));
    }

    @PostMapping("/events/release-lock")
    public ResponseEntity<ApiResponse<String>> releaseLock(@RequestBody EventReleaseLockRequestDto request) {
        eventBookingService.releaseLock(request.getBookingId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Lock released", "OK"));
    }
}
