package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.model.Booking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BookingEmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Async
    public void sendBookingConfirmedEmail(String toEmail, Booking booking) {
        if (toEmail == null || toEmail.isBlank()) {
            return;
        }

        if (mailSender == null) {
            log.warn("Mail sender is not configured. Skipping booking confirmation email for bookingRef={}", booking.getBookingReference());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Booking Confirmed - " + booking.getBookingReference());
            message.setText(buildEmailBody(booking));
            mailSender.send(message);

            log.info("Booking confirmation email sent for bookingRef={} to={}", booking.getBookingReference(), toEmail);
        } catch (Exception ex) {
            log.error("Failed to send confirmation email for bookingRef={}: {}", booking.getBookingReference(), ex.getMessage(), ex);
        }
    }

    private String buildEmailBody(Booking booking) {
        String movieName = booking.getShow() != null && booking.getShow().getMovie() != null
                ? booking.getShow().getMovie().getTitle()
                : "Movie";

        String theaterName = booking.getShow() != null && booking.getShow().getTheater() != null
                ? booking.getShow().getTheater().getName()
                : "Theater";

        String date = booking.getShow() != null && booking.getShow().getShowDate() != null
                ? booking.getShow().getShowDate().toString()
                : "";

        String time = booking.getShow() != null && booking.getShow().getShowTime() != null
                ? booking.getShow().getShowTime().toString()
                : "";

        return "Your movie ticket has been confirmed.\n\n"
                + "Booking Ref: " + booking.getBookingReference() + "\n"
                + "Movie: " + movieName + "\n"
                + "Theater: " + theaterName + "\n"
                + "Date: " + date + "\n"
                + "Time: " + time + "\n"
                + "Seats: " + booking.getSeatLabels() + "\n"
                + "Amount: " + booking.getTotalAmount() + "\n"
                + "Payment ID: " + booking.getRazorpayPaymentId() + "\n\n"
                + "Enjoy your show!";
    }
}
