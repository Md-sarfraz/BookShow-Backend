package com.jwtAuthentication.jwt.controller;

import com.jwtAuthentication.jwt.model.Booking;
import com.jwtAuthentication.jwt.model.User;
import com.jwtAuthentication.jwt.repository.BookingRepository;
import com.jwtAuthentication.jwt.repository.UserRepository;
import com.jwtAuthentication.jwt.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/debug")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DebugMailController {

    private final JavaMailSender javaMailSender;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.port:}")
    private String mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${app.mail.from:}")
    private String mailFrom;

    @Value("${spring.mail.properties.mail.smtp.auth:}")
    private String smtpAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:}")
    private String starttlsEnable;

    @Value("${spring.mail.properties.mail.smtp.starttls.required:}")
    private String starttlsRequired;

    /**
     * Test endpoint to verify mail configuration
     * GET /api/v1/debug/mail-config
     */
    @GetMapping("/mail-config")
    public ResponseEntity<Map<String, Object>> getMailConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("mailHost", mailHost);
        config.put("mailPort", mailPort);
        config.put("mailUsername", maskEmail(mailUsername));
        config.put("mailFrom", maskEmail(mailFrom));
        config.put("smtpAuth", smtpAuth);
        config.put("starttlsEnable", starttlsEnable);
        config.put("starttlsRequired", starttlsRequired);
        config.put("javaMailSenderAvailable", javaMailSender != null);
        
        log.info("Mail configuration: {}", config);
        return ResponseEntity.ok(config);
    }

    @PostMapping("/send-test")
    public ResponseEntity<Map<String, Object>> sendTestMail(@RequestParam String to) {
        Map<String, Object> response = new HashMap<>();

        try {
            jakarta.mail.internet.MimeMessage message = javaMailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setFrom(mailFrom != null && !mailFrom.isBlank() ? mailFrom : mailUsername);
            helper.setSubject("BookShow SMTP Test");
            helper.setText("This is a test email from BookShow. If you received this, SMTP is working.", false);

            javaMailSender.send(message);

            response.put("success", true);
            response.put("message", "Test email sent successfully");
            response.put("to", to);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Failed to send test email to {}: {}", to, ex.getMessage(), ex);
            response.put("success", false);
            response.put("message", "Failed to send test email: " + ex.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/resend-booking-ticket")
    public ResponseEntity<Map<String, Object>> resendBookingTicket(@RequestParam String bookingReference) {
        Map<String, Object> response = new HashMap<>();

        try {
            Booking booking = bookingRepository.findByBookingReference(bookingReference)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found for reference: " + bookingReference));

            if (booking.getPaymentStatus() != Booking.PaymentStatus.CONFIRMED) {
                response.put("success", false);
                response.put("message", "Booking is not CONFIRMED. Current status: " + booking.getPaymentStatus());
                response.put("bookingReference", bookingReference);
                return ResponseEntity.badRequest().body(response);
            }

            if (booking.getUserId() == null) {
                response.put("success", false);
                response.put("message", "Booking has no userId. Cannot resolve recipient email.");
                response.put("bookingReference", bookingReference);
                return ResponseEntity.badRequest().body(response);
            }

            User user = userRepository.findById(booking.getUserId().intValue())
                    .orElseThrow(() -> new IllegalArgumentException("User not found for userId: " + booking.getUserId()));

            if (user.getEmail() == null || user.getEmail().isBlank()) {
                response.put("success", false);
                response.put("message", "User email is empty for userId: " + booking.getUserId());
                response.put("bookingReference", bookingReference);
                return ResponseEntity.badRequest().body(response);
            }

            emailService.sendBookingConfirmationTicket(user.getEmail(), booking);

            response.put("success", true);
            response.put("message", "Ticket email queued for sending");
            response.put("bookingReference", bookingReference);
            response.put("recipient", user.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Failed to resend booking ticket for reference {}: {}", bookingReference, ex.getMessage(), ex);
            response.put("success", false);
            response.put("message", "Failed to resend booking ticket: " + ex.getMessage());
            response.put("bookingReference", bookingReference);
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "[EMPTY]";
        }
        if (email.length() <= 4) {
            return "****";
        }
        return email.substring(0, 2) + "***" + email.substring(email.length() - 2);
    }
}
