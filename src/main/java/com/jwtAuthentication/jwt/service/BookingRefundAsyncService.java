package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.model.Booking;
import com.jwtAuthentication.jwt.model.Refund;
import com.jwtAuthentication.jwt.repository.BookingRepository;
import com.jwtAuthentication.jwt.repository.RefundRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class BookingRefundAsyncService {

    private final RefundRepository refundRepository;
    private final BookingRepository bookingRepository;
    private final RefundProcessor refundProcessor;

    public BookingRefundAsyncService(
            RefundRepository refundRepository,
            BookingRepository bookingRepository,
            RefundProcessor refundProcessor
    ) {
        this.refundRepository = refundRepository;
        this.bookingRepository = bookingRepository;
        this.refundProcessor = refundProcessor;
    }

    @Async
    @Transactional
    public void processRefundAsync(Long refundId, Long bookingId, double refundAmount) {
        try {
            Refund refund = refundRepository.findById(refundId)
                    .orElseThrow(() -> new IllegalStateException("Refund not found for id: " + refundId));

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new IllegalStateException("Booking not found for id: " + bookingId));

            RefundResult result = refundProcessor.initiateRefund(booking, refundAmount);
            if (result.successful()) {
                refund.setRefundStatus(Refund.RefundStatus.SUCCESS);
                refund.setProviderReference(result.providerReference());
                refund.setFailureReason(null);
            } else {
                refund.setRefundStatus(Refund.RefundStatus.FAILED);
                refund.setFailureReason(result.errorMessage());
            }
            refundRepository.save(refund);
        } catch (Exception ex) {
            log.error("Async refund failed. refundId={} bookingId={}", refundId, bookingId, ex);
            refundRepository.findById(refundId).ifPresent(refund -> {
                refund.setRefundStatus(Refund.RefundStatus.FAILED);
                refund.setFailureReason("Refund initiation failed: " + ex.getMessage());
                refundRepository.save(refund);
            });
        }
    }
}
