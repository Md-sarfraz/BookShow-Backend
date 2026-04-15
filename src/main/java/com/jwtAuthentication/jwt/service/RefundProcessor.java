package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.model.Booking;

public interface RefundProcessor {
    RefundResult initiateRefund(Booking booking, double refundAmount);
}
