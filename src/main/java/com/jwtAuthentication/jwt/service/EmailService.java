package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.model.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final QRCodeService qrCodeService;
    private final TicketPdfService ticketPdfService;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.mail.from:${spring.mail.username:}}")
    private String fromEmail;

    @Async
    public void sendBookingConfirmationTicket(String toEmail, Booking booking) {
        if (toEmail == null || toEmail.isBlank() || booking == null) {
            log.warn("❌ Invalid email or booking. email={} bookingRef={}", toEmail, booking != null ? booking.getBookingReference() : "null");
            return;
        }

        if (booking.getPaymentStatus() != Booking.PaymentStatus.CONFIRMED) {
            log.info("⏭️ Skipping ticket email because booking is not CONFIRMED. bookingRef={} status={}",
                    booking.getBookingReference(), booking.getPaymentStatus());
            return;
        }

        // Validate email configuration BEFORE attempting to send
        if (fromEmail == null || fromEmail.isBlank()) {
            log.error("❌ Email configuration error: fromEmail is not set. MAIL_FROM or MAIL_USERNAME must be configured in .env");
            return;
        }

        log.info("📧 Starting email send. bookingRef={} toEmail={} fromEmail={} paymentStatus={}", 
                booking.getBookingReference(), toEmail, fromEmail, booking.getPaymentStatus());

        try {
            log.debug("🔷 Generating QR code for bookingRef={}", booking.getBookingReference());
            byte[] qrPng = qrCodeService.generatePngBytes(booking);
            log.debug("🔷 QR code generated: {} bytes", qrPng.length);

            log.debug("📄 Generating PDF ticket for bookingRef={}", booking.getBookingReference());
            byte[] ticketPdf = ticketPdfService.generateTicketPdf(booking, qrPng);
            log.debug("📄 PDF ticket generated: {} bytes", ticketPdf.length);

            if (ticketPdf.length == 0) {
                log.warn("⚠️ PDF ticket is empty for bookingRef={}", booking.getBookingReference());
            }

            log.debug("✉️ Creating MIME message for bookingRef={}", booking.getBookingReference());
            jakarta.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setFrom(fromEmail);
            log.debug("✉️ Email from={} to={}", fromEmail, toEmail);

            helper.setSubject("TicketFlix 🎬 | Booking Confirmed " + safe(booking.getBookingReference()));
            String htmlContent = emailTemplateService.buildBookingConfirmationHtml(booking);
            helper.setText(htmlContent, true);
            log.debug("✉️ Email HTML content set");

            helper.addInline("qrCode", new ByteArrayResource(qrPng), "image/png");
            log.debug("✉️ QR inline image added");

            if (ticketPdf.length > 0) {
                helper.addAttachment(
                        "Ticket-" + safe(booking.getBookingReference()) + ".pdf",
                        new ByteArrayResource(ticketPdf),
                        "application/pdf"
                );
                log.debug("✉️ PDF attachment added: {} bytes", ticketPdf.length);
            }

            log.info("🚀 Sending MIME message for bookingRef={}", booking.getBookingReference());
            javaMailSender.send(mimeMessage);
            log.info("✅✅✅ Booking ticket email sent successfully! bookingRef={} to={}", booking.getBookingReference(), toEmail);
        } catch (Exception ex) {
            // Must not impact booking flow.
            log.error("❌❌❌ Failed to send booking ticket email. bookingRef={} to={} error={} cause={}", 
                    booking.getBookingReference(), toEmail, ex.getMessage(), ex.getClass().getSimpleName(), ex);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
