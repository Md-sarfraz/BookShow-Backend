package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.model.Booking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailTemplateService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    @Value("${app.support.email:support@ticketflix.com}")
    private String supportEmail;

    public String buildBookingConfirmationHtml(Booking booking) {
        String movieName = booking.getShow() != null && booking.getShow().getMovie() != null
                ? safe(booking.getShow().getMovie().getTitle()) : "Movie";
        String theaterName = booking.getShow() != null && booking.getShow().getTheater() != null
                ? safe(booking.getShow().getTheater().getName()) : "Theater";
        String screen = booking.getShow() != null && booking.getShow().getScreenNumber() != null
                ? safe(booking.getShow().getScreenNumber()) : "Screen 1";
        String showDate = booking.getShow() != null && booking.getShow().getShowDate() != null
                ? booking.getShow().getShowDate().format(DATE_FORMATTER) : "N/A";
        String showTime = booking.getShow() != null && booking.getShow().getShowTime() != null
                ? booking.getShow().getShowTime().format(TIME_FORMATTER) : "N/A";
        String bookingRef = safe(booking.getBookingReference());
        String totalAmount = booking.getTotalAmount() == null ? "0.00" : String.format("%.2f", booking.getTotalAmount());
        String posterUrl = booking.getShow() != null && booking.getShow().getMovie() != null
                ? safe(booking.getShow().getMovie().getPostUrl()) : "";

        String seatBadges = buildSeatBadges(safe(booking.getSeatLabels()));
        String posterSection = posterUrl.isBlank()
                ? ""
                : "<div style='text-align:center;margin:12px 0 18px 0;'>"
                + "<img src='" + escapeHtml(posterUrl) + "' alt='Movie Poster' style='width:100%;max-width:540px;border-radius:14px;display:block;margin:0 auto;border:1px solid #e7e7ef;'/>"
                + "</div>";

        return ""
                + "<html><body style='margin:0;padding:0;background:#f4f5f8;font-family:Arial,Helvetica,sans-serif;color:#1a1a2e;'>"
                + "<div style='max-width:680px;margin:0 auto;padding:20px 12px;'>"
                + "<div style='border-radius:18px;overflow:hidden;box-shadow:0 10px 35px rgba(22,23,46,0.18);background:#ffffff;'>"
                + "<div style='padding:26px 22px;background:linear-gradient(135deg,#0f1024 0%,#1b1d49 45%,#c31f4d 100%);color:#ffffff;'>"
                + "<div style='font-size:28px;font-weight:800;letter-spacing:0.4px;'>TicketFlix 🎬</div>"
                + "<div style='margin-top:6px;font-size:14px;opacity:0.9;'>Your booking is confirmed. Enjoy the show!</div>"
                + "</div>"

                + "<div style='padding:22px;'>"
                + posterSection
                + "<div style='margin:0 0 14px 0;padding:10px 14px;background:#e9f9ee;border-radius:10px;color:#087f39;font-size:13px;font-weight:700;display:inline-block;'>CONFIRMED ✅</div>"

                + "<div style='font-size:26px;font-weight:800;color:#171935;line-height:1.2;margin-bottom:14px;'>" + escapeHtml(movieName) + "</div>"

                + "<div style='border:1px solid #ebeaf4;border-radius:16px;overflow:hidden;background:#fcfcff;'>"
                + "<table role='presentation' width='100%' style='border-collapse:collapse;'>"
                + "<tr><td style='padding:12px 16px;color:#6a6f82;font-size:12px;'>Theater</td><td style='padding:12px 16px;text-align:right;font-weight:700;font-size:14px;color:#1a1a2e;'>" + escapeHtml(theaterName) + " • " + escapeHtml(screen) + "</td></tr>"
                + "<tr><td style='padding:12px 16px;color:#6a6f82;font-size:12px;border-top:1px solid #ececf4;'>Date & Time</td><td style='padding:12px 16px;text-align:right;font-weight:700;font-size:14px;color:#1a1a2e;border-top:1px solid #ececf4;'>" + escapeHtml(showDate) + " • " + escapeHtml(showTime) + "</td></tr>"
                + "<tr><td style='padding:12px 16px;color:#6a6f82;font-size:12px;border-top:1px solid #ececf4;'>Seats</td><td style='padding:12px 16px;text-align:right;border-top:1px solid #ececf4;'>" + seatBadges + "</td></tr>"
                + "<tr><td style='padding:12px 16px;color:#6a6f82;font-size:12px;border-top:1px solid #ececf4;'>Total Price</td><td style='padding:12px 16px;text-align:right;font-weight:800;font-size:16px;color:#c31f4d;border-top:1px solid #ececf4;'>Rs. " + escapeHtml(totalAmount) + "</td></tr>"
                + "<tr><td style='padding:12px 16px;color:#6a6f82;font-size:12px;border-top:1px solid #ececf4;'>Booking ID</td><td style='padding:12px 16px;text-align:right;font-weight:700;font-size:13px;color:#1a1a2e;border-top:1px solid #ececf4;'>" + escapeHtml(bookingRef) + "</td></tr>"
                + "</table>"
                + "</div>"

                + "<div style='margin-top:18px;padding:16px;border-radius:14px;border:1px dashed #cfd2e4;background:#f8f9ff;text-align:center;'>"
                + "<div style='font-size:13px;font-weight:700;color:#555d78;margin-bottom:8px;'>Scan at entry</div>"
                + "<img src='cid:qrCode' alt='QR Code' style='width:170px;height:170px;border-radius:10px;border:1px solid #d6d9ea;background:#ffffff;padding:8px;'/>"
                + "</div>"

                + "<div style='margin-top:16px;background:#171935;color:#ffffff;border-radius:12px;padding:13px 15px;text-align:center;font-weight:700;font-size:14px;'>Show this ticket at the theater</div>"
                + "</div>"

                + "<div style='padding:16px 22px;border-top:1px solid #ececf4;color:#6d7288;font-size:12px;background:#fafbff;'>"
                + "Thank you for booking with TicketFlix. Need help? Contact us at " + escapeHtml(supportEmail) + ""
                + "</div>"
                + "</div>"
                + "</div>"
                + "<style>@media only screen and (max-width:600px){ .seat-chip{margin-bottom:6px !important;} }</style>"
                + "</body></html>";
    }

    private String buildSeatBadges(String seatLabels) {
        if (seatLabels == null || seatLabels.isBlank()) {
            return "<span style='font-size:13px;font-weight:700;color:#1a1a2e;'>N/A</span>";
        }

        String[] seats = seatLabels.split(",");
        StringBuilder chips = new StringBuilder();
        for (String seat : seats) {
            String trimmed = seat == null ? "" : seat.trim();
            if (!trimmed.isEmpty()) {
                chips.append("<span class='seat-chip' style='display:inline-block;background:#eef1ff;border:1px solid #d6dbff;color:#2b336e;padding:4px 9px;border-radius:999px;font-size:12px;font-weight:700;margin-left:6px;'>")
                        .append(escapeHtml(trimmed))
                        .append("</span>");
            }
        }

        if (chips.length() == 0) {
            return "<span style='font-size:13px;font-weight:700;color:#1a1a2e;'>N/A</span>";
        }
        return chips.toString();
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