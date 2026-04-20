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
public class QRCodeService {

    public String generateBase64Png(Booking booking) {
        byte[] qrBytes = generatePngBytes(booking);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(qrBytes);
    }

    public byte[] generatePngBytes(Booking booking) {
        String qrPayload = buildQrPayload(booking);

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(qrPayload, BarcodeFormat.QR_CODE, 280, 280);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return output.toByteArray();
        } catch (WriterException | java.io.IOException ex) {
            String fallback = "BOOKING_REF=" + nullSafe(booking.getBookingReference());
            return fallback.getBytes(StandardCharsets.UTF_8);
        }
    }

    private String buildQrPayload(Booking booking) {
        String raw = "TICKETFLIX|BOOKING_ID=" + nullSafe(booking.getBookingReference());
        return new String(raw.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
