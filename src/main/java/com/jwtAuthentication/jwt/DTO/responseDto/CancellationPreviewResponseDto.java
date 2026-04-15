package com.jwtAuthentication.jwt.DTO.responseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancellationPreviewResponseDto {
    private boolean cancellationAllowed;
    private String message;
    private Long bookingId;
    private Double refundableAmount;
    private Double refundAmount;
    private Double convenienceFeeDeducted;
    private Double refundPercentage;
    private String refundStatus;
    private String showDateTime;
}
