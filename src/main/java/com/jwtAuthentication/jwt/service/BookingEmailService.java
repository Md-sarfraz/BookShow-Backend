package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.model.Booking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Slf4j
@Service
public class BookingEmailService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.from:${spring.mail.username:}}")
    private String fromEmail;

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
            jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Booking Confirmed - " + safe(booking.getBookingReference()));

            if (fromEmail != null && !fromEmail.isBlank()) {
                helper.setFrom(fromEmail);
            }

            helper.setText(buildEmailHtml(booking), true);

            if (booking.getQrCode() != null && booking.getQrCode().startsWith("data:image/png;base64,")) {
                byte[] imageBytes = Base64.getDecoder().decode(
                        booking.getQrCode().replace("data:image/png;base64,", "")
                );
                helper.addInline("ticketQr", new ByteArrayResource(imageBytes), "image/png");
            }

            log.info(
                    "Sending booking confirmation email. bookingRef={} to={} paymentId={}",
                    booking.getBookingReference(),
                    toEmail,
                    booking.getRazorpayPaymentId()
            );

            mailSender.send(mimeMessage);
            log.info("Booking confirmation email sent for bookingRef={} to={}", booking.getBookingReference(), toEmail);
        } catch (Exception ex) {
            log.error("Failed to send confirmation email for bookingRef={}: {}", booking.getBookingReference(), ex.getMessage(), ex);
        }
    }

    private String buildEmailHtml(Booking booking) {
        String movieName = booking.getShow() != null && booking.getShow().getMovie() != null
                ? booking.getShow().getMovie().getTitle()
                : "Movie";

        String theaterName = booking.getShow() != null && booking.getShow().getTheater() != null
                ? booking.getShow().getTheater().getName()
                : "Theater";

        String date = booking.getShow() != null && booking.getShow().getShowDate() != null
                ? booking.getShow().getShowDate().format(DATE_FORMATTER)
                : "";

        String time = booking.getShow() != null && booking.getShow().getShowTime() != null
                ? booking.getShow().getShowTime().format(TIME_FORMATTER)
                : "";

        String seats = safe(booking.getSeatLabels()).replace(",", ", ");
        String bookingRef = safe(booking.getBookingReference());
        String paymentId = safe(booking.getRazorpayPaymentId());
        String totalAmount = booking.getTotalAmount() == null ? "0.00" : String.format("%.2f", booking.getTotalAmount());

        String qrSection = "";
        if (booking.getQrCode() != null && booking.getQrCode().startsWith("data:image/png;base64,")) {
            qrSection = "<div style='margin-top:18px;text-align:center;'>"
                    + "<p style='margin:0 0 8px;color:#4b5563;font-size:12px;'>Scan this QR at entry</p>"
                    + "<img src='cid:ticketQr' alt='Ticket QR Code' style='width:170px;height:170px;border:1px solid #e5e7eb;padding:6px;border-radius:10px;background:#fff;'/>"
                    + "</div>";
        }

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='margin:0;padding:0;background:#f3f4f6;font-family:Arial,sans-serif;color:#111827;'>")
                .append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='padding:22px 0;'><tr><td align='center'>")
                .append("<table role='presentation' width='620' cellpadding='0' cellspacing='0' style='max-width:620px;background:#ffffff;border-radius:14px;overflow:hidden;border:1px solid #e5e7eb;'>")
                .append("<tr><td style='padding:18px 22px;background:#111827;color:#ffffff;'>")
                .append("<h2 style='margin:0;font-size:20px;'>Your Ticket is Confirmed</h2>")
                .append("<p style='margin:6px 0 0;font-size:13px;color:#d1d5db;'>Booking ID: ")
                .append(escapeHtml(bookingRef))
                .append("</p></td></tr>")
                .append("<tr><td style='padding:22px;'>")
                .append("<p style='margin:0 0 14px;font-size:14px;color:#374151;'>Thanks for booking with BookShow. Here are your ticket details:</p>")
                .append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='font-size:14px;border-collapse:collapse;'>")
                .append(detailRow("Movie", movieName))
                .append(detailRow("Theater", theaterName))
                .append(detailRow("Date", date))
                .append(detailRow("Time", time))
                .append(detailRow("Seat Numbers", seats))
                .append(detailRow("Booking ID", bookingRef))
                .append(detailRow("Payment ID", paymentId))
                .append(detailRow("Paid Amount", "Rs. " + totalAmount))
                .append("</table>")
                .append(qrSection)
                .append("</td></tr>")
                .append("<tr><td style='padding:14px 22px;background:#f9fafb;color:#6b7280;font-size:12px;'>")
                .append("Please keep this email for entry and support queries. Enjoy your show.")
                .append("</td></tr>")
                .append("</table></td></tr></table></body></html>");

        return html.toString();
    }

    private String detailRow(String label, String value) {
        return "<tr>"
                + "<td style='padding:9px 0;color:#6b7280;width:170px;'>" + escapeHtml(label) + "</td>"
                + "<td style='padding:9px 0;font-weight:600;'>" + escapeHtml(value) + "</td>"
                + "</tr>";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String escapeHtml(String value) {
        return safe(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
