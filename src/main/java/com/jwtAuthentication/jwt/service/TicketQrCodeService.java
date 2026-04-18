package com.jwtAuthentication.jwt.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.jwtAuthentication.jwt.model.Booking;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class TicketQrCodeService {

    public String generateBase64Png(Booking booking) {
        String qrPayload = buildQrPayload(booking);

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(qrPayload, BarcodeFormat.QR_CODE, 280, 280);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            String base64 = Base64.getEncoder().encodeToString(output.toByteArray());
            return "data:image/png;base64," + base64;
        } catch (WriterException | java.io.IOException ex) {
            return "QR|BOOKING=" + booking.getBookingReference();
        }
    }

    private String buildQrPayload(Booking booking) {
        String movieTitle = booking.getShow() != null && booking.getShow().getMovie() != null
                ? booking.getShow().getMovie().getTitle()
                : "";

        String showDate = booking.getShow() != null && booking.getShow().getShowDate() != null
                ? booking.getShow().getShowDate().toString()
                : "";

        String showTime = booking.getShow() != null && booking.getShow().getShowTime() != null
                ? booking.getShow().getShowTime().toString()
                : "";

        String raw = "BOOKING_REF=" + booking.getBookingReference()
                + "|ORDER_ID=" + nullSafe(booking.getRazorpayOrderId())
                + "|PAYMENT_ID=" + nullSafe(booking.getRazorpayPaymentId())
                + "|MOVIE=" + movieTitle
                + "|SHOW_DATE=" + showDate
                + "|SHOW_TIME=" + showTime
                + "|SEATS=" + nullSafe(booking.getSeatLabels())
                + "|AMOUNT=" + booking.getTotalAmount();

        return new String(raw.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
