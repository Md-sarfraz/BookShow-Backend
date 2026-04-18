package com.jwtAuthentication.jwt.DTO;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private Long showId;
    private List<String> seatLabels;
    private Long userId; // set server-side from authenticated JWT principal
}
