package com.jwtAuthentication.jwt.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Refund {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long refundId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "booking_id", nullable = false)
	private Booking booking;

	@Column(name = "paid_amount")
	private Double paidAmount;

	@Column(name = "refundable_amount")
	private Double refundableAmount;

	@Column(name = "refund_amount", nullable = false)
	private Double refundAmount;

	@Column(name = "convenience_fee_deducted")
	private Double convenienceFeeDeducted;

	@Column(name = "refund_percentage")
	private Double refundPercentage;

	@Column(name = "payment_provider")
	private String paymentProvider;

	@Column(name = "provider_reference")
	private String providerReference;

	@Column(name = "failure_reason")
	private String failureReason;

	@Enumerated(EnumType.STRING)
	@Column(name = "refund_status", nullable = false)
	private RefundStatus refundStatus = RefundStatus.PENDING;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public enum RefundStatus {
		PENDING,
		SUCCESS,
		FAILED
	}
}
