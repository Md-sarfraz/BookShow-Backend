package com.jwtAuthentication.jwt.util;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean status;   // success / failure
    private String message;   // human readable
    private T data;           // actual payload

    // Explicit constructor
    public ApiResponse(boolean status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
