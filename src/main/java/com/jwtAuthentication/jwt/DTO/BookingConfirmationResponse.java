package com.jwtAuthentication.jwt.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingConfirmationResponse {
    private Long    bookingId;
    private String  bookingReference;
    private String  razorpayPaymentId;
    private Double  totalAmount;
    private Double  baseAmount;
    private Double  convenienceFee;
    private Double  discount;
    private Integer numberOfSeats;
    private String  seatLabels;       // comma-separated
    private String  movieTitle;
    private String  theaterName;
    private String  theaterCity;
    private String  showDate;
    private String  showTime;
    private String  paymentStatus;
}
