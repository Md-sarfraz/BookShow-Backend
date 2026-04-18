package com.jwtAuthentication.jwt.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    // User who booked (nullable for guest checkout)
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    // Comma-separated seat labels, e.g. "A1,A2,B5"
    @Column(name = "seat_labels", nullable = false)
    private String seatLabels;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "base_amount", nullable = false)
    private Double baseAmount;

    @Column(name = "convenience_fee")
    private Double convenienceFee;

    @Column(name = "discount")
    private Double discount;

    @Column(name = "number_of_seats", nullable = false)
    private Integer numberOfSeats;

    // Razorpay order ID (from server-side order creation)
    @Column(name = "razorpay_order_id", unique = true)
    private String razorpayOrderId;

    // Razorpay payment ID (after successful payment)
    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Lob
    @Column(name = "qr_code", columnDefinition = "LONGTEXT")
    private String qrCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "booking_reference", unique = true)
    private String bookingReference; // e.g. BTS12345678

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (bookingReference == null) {
            bookingReference = "BTS" + System.currentTimeMillis();
        }
    }

    public enum PaymentStatus {
        PENDING,    // Order created, payment not done yet
        CONFIRMED,  // Payment verified and successful
        FAILED,     // Payment failed
        EXPIRED,    // Payment window elapsed before successful payment
        CANCELLED   // Booking cancelled
    }
}

