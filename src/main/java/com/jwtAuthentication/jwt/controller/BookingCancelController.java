package com.jwtAuthentication.jwt.controller;

import com.jwtAuthentication.jwt.DTO.responseDto.CancelBookingResponseDto;
import com.jwtAuthentication.jwt.repository.UserRepository;
import com.jwtAuthentication.jwt.service.BookingCancellationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/booking")
@CrossOrigin(origins = "*")
public class BookingCancelController {

    @Autowired
    private BookingCancellationService bookingCancellationService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/cancel/{bookingId}")
    public ResponseEntity<CancelBookingResponseDto> cancelBookingByPath(
            @PathVariable("bookingId") Long bookingId,
            Authentication authentication
    ) {
        try {
            Long requesterId = resolveCurrentUserId(authentication);
            boolean isAdmin = isAdmin(authentication);

            CancelBookingResponseDto response = bookingCancellationService.cancelBooking(
                    bookingId,
                    requesterId,
                    isAdmin
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new CancelBookingResponseDto(
                    false,
                    ex.getMessage(),
                    bookingId,
                    null,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    "FAILED",
                    null,
                    null
            ));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CancelBookingResponseDto(
                    false,
                    ex.getMessage(),
                    bookingId,
                    null,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    "FAILED",
                    null,
                    null
            ));
        }
    }

    private Long resolveCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication is required");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(user -> (long) user.getId())
                .orElseThrow(() -> new IllegalStateException("Unable to resolve current user"));
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
