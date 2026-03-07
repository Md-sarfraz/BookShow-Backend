package com.jwtAuthentication.jwt.DTO;

public class SeatStatusDTO {

    private String seatLabel;
    /** One of: AVAILABLE, LOCKED, BOOKED */
    private String status;

    public SeatStatusDTO(String seatLabel, String status) {
        this.seatLabel = seatLabel;
        this.status = status;
    }

    public String getSeatLabel() { return seatLabel; }
    public void setSeatLabel(String seatLabel) { this.seatLabel = seatLabel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
