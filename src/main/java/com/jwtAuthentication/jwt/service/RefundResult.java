package com.jwtAuthentication.jwt.service;

public record RefundResult(
        boolean successful,
        String providerReference,
        String errorMessage
) {
}
