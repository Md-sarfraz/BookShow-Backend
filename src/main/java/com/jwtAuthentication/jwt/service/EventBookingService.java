package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.DTO.requestDto.EventBookingRequestDto;
import com.jwtAuthentication.jwt.DTO.requestDto.EventConfirmBookingRequestDto;
import com.jwtAuthentication.jwt.DTO.requestDto.EventPaymentOrderRequestDto;
import com.jwtAuthentication.jwt.DTO.responseDto.EventBookingResponseDto;
import com.jwtAuthentication.jwt.DTO.responseDto.EventAvailabilityResponseDto;
import com.jwtAuthentication.jwt.DTO.responseDto.EventConfirmBookingResponseDto;
import com.jwtAuthentication.jwt.DTO.responseDto.EventLockTicketsResponseDto;
import com.jwtAuthentication.jwt.DTO.responseDto.EventPaymentOrderResponseDto;
import com.jwtAuthentication.jwt.DTO.responseDto.EventTicketDetailsResponseDto;
import com.jwtAuthentication.jwt.model.Event;
import com.jwtAuthentication.jwt.model.EventBooking;
import com.jwtAuthentication.jwt.model.EventTicket;
import com.jwtAuthentication.jwt.repository.EventBookingRepository;
import com.jwtAuthentication.jwt.repository.EventRepository;
import com.jwtAuthentication.jwt.repository.EventTicketRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EventBookingService {

    private static final int LOCK_MINUTES = 10;

    private final EventRepository eventRepository;
    private final EventBookingRepository eventBookingRepository;
    private final EventTicketRepository eventTicketRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Transactional
    public EventBookingResponseDto createBooking(EventBookingRequestDto request) {
        if (request.getEventId() == null) {
            throw new RuntimeException("eventId is required");
        }
        if (request.getUserId() == null) {
            throw new RuntimeException("userId is required");
        }

        int numberOfTickets = request.getNumberOfTickets() == null ? 1 : request.getNumberOfTickets();
        if (numberOfTickets <= 0) {
            throw new RuntimeException("numberOfTickets must be greater than 0");
        }

        cleanupExpiredLocks();

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found with id : " + request.getEventId()));

        long remainingTickets = getRemainingTickets(event.getId());
        if (remainingTickets < numberOfTickets) {
            throw new RuntimeException("Not enough tickets available. Remaining: " + remainingTickets);
        }

        double unitPrice = parsePrice(event.getPrice());
        double totalAmount = unitPrice * numberOfTickets;

        EventBooking booking = new EventBooking();
        booking.setUserId(request.getUserId());
        booking.setEvent(event);
        booking.setNumberOfTickets(numberOfTickets);
        booking.setUnitPrice(unitPrice);
        booking.setTotalAmount(totalAmount);
        booking.setBookingStatus(EventBooking.BookingStatus.CONFIRMED);
        booking.setLockExpiresAt(null);
        booking.setPaymentId("DIRECT-" + System.currentTimeMillis());

        EventBooking saved = eventBookingRepository.save(booking);
        saved.setQrCode(buildQrCodeValue(event.getId(), saved.getBookingReference()));
        EventBooking updated = eventBookingRepository.save(saved);
        return toDto(updated);
    }

    @Transactional
    public EventAvailabilityResponseDto checkAvailability(Integer eventId, Integer ticketCount) {
        validateTicketCount(ticketCount);
        Event event = getEventOrThrow(eventId);
        cleanupExpiredLocks();

        long remainingTickets = getRemainingTickets(event.getId());
        return new EventAvailabilityResponseDto(remainingTickets >= ticketCount, remainingTickets);
    }

    @Transactional
    public EventLockTicketsResponseDto lockTickets(Integer eventId, Long userId, Integer ticketCount) {
        if (userId == null) {
            throw new RuntimeException("userId is required");
        }
        validateTicketCount(ticketCount);

        Event event = getEventOrThrow(eventId);
        cleanupExpiredLocks();

        long remainingTickets = getRemainingTickets(event.getId());
        if (remainingTickets < ticketCount) {
            throw new RuntimeException("Only " + remainingTickets + " tickets are available");
        }

        double unitPrice = parsePrice(event.getPrice());
        double totalAmount = unitPrice * ticketCount;
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(LOCK_MINUTES);

        EventBooking booking = new EventBooking();
        booking.setUserId(userId);
        booking.setEvent(event);
        booking.setNumberOfTickets(ticketCount);
        booking.setUnitPrice(unitPrice);
        booking.setTotalAmount(totalAmount);
        booking.setBookingStatus(EventBooking.BookingStatus.LOCKED);
        booking.setLockExpiresAt(expiresAt);

        EventBooking saved = eventBookingRepository.save(booking);
        return new EventLockTicketsResponseDto(
                saved.getBookingId(),
                saved.getBookingReference(),
                saved.getLockExpiresAt(),
                saved.getUnitPrice(),
                saved.getTotalAmount()
        );
    }

    @Transactional
    public EventPaymentOrderResponseDto createPaymentOrder(EventPaymentOrderRequestDto request) throws RazorpayException {
        if (request.getBookingId() == null) {
            throw new RuntimeException("bookingId is required");
        }

        EventBooking booking = eventBookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        cleanupExpiredLocks();

        if (booking.getBookingStatus() != EventBooking.BookingStatus.LOCKED) {
            throw new RuntimeException("Booking is not in locked state");
        }
        if (booking.getLockExpiresAt() == null || booking.getLockExpiresAt().isBefore(LocalDateTime.now())) {
            booking.setBookingStatus(EventBooking.BookingStatus.EXPIRED);
            eventBookingRepository.save(booking);
            throw new RuntimeException("Booking lock has expired");
        }

        long amountInPaise = Math.round(booking.getTotalAmount() * 100);

        RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "EVT_" + booking.getBookingReference());
        orderRequest.put("payment_capture", 1);

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        String razorpayOrderId = razorpayOrder.get("id");

        booking.setRazorpayOrderId(razorpayOrderId);
        eventBookingRepository.save(booking);

        return new EventPaymentOrderResponseDto(
                razorpayOrderId,
                amountInPaise,
                "INR",
                razorpayKeyId,
                booking.getBookingId(),
                booking.getBookingReference()
        );
    }

    @Transactional
    public EventConfirmBookingResponseDto confirmBooking(EventConfirmBookingRequestDto request)
            throws NoSuchAlgorithmException, InvalidKeyException {
        if (request.getRazorpayOrderId() == null || request.getRazorpayPaymentId() == null || request.getRazorpaySignature() == null) {
            throw new RuntimeException("razorpayOrderId, razorpayPaymentId and razorpaySignature are required");
        }

        String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);

        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        String computedSignature = HexFormat.of().formatHex(hash);

        if (!computedSignature.equals(request.getRazorpaySignature())) {
            throw new SecurityException("Payment signature verification failed");
        }

        EventBooking booking = eventBookingRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Booking not found for orderId: " + request.getRazorpayOrderId()));

        cleanupExpiredLocks();

        if (booking.getBookingStatus() == EventBooking.BookingStatus.CONFIRMED) {
            EventTicket existingTicket = eventTicketRepository.findByBookingBookingId(booking.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Ticket record not found for confirmed booking"));
            return new EventConfirmBookingResponseDto(existingTicket.getTicketId(), existingTicket.getQrCode(), toDto(booking));
        }

        if (booking.getBookingStatus() != EventBooking.BookingStatus.LOCKED) {
            throw new RuntimeException("Booking is not eligible for confirmation");
        }
        if (booking.getLockExpiresAt() == null || booking.getLockExpiresAt().isBefore(LocalDateTime.now())) {
            booking.setBookingStatus(EventBooking.BookingStatus.EXPIRED);
            eventBookingRepository.save(booking);
            throw new RuntimeException("Booking lock expired. Please start again.");
        }

        booking.setCustomerName(request.getName());
        booking.setCustomerEmail(request.getEmail());
        booking.setCustomerPhone(request.getPhone());
        booking.setPaymentId(request.getRazorpayPaymentId());
        booking.setBookingStatus(EventBooking.BookingStatus.CONFIRMED);
        booking.setLockExpiresAt(null);

        String qrCode = buildQrCodeValue(booking.getEvent().getId(), booking.getBookingReference());
        booking.setQrCode(qrCode);
        EventBooking confirmedBooking = eventBookingRepository.save(booking);

        EventTicket ticket = new EventTicket();
        ticket.setBooking(confirmedBooking);
        ticket.setQrCode(qrCode);
        EventTicket savedTicket = eventTicketRepository.save(ticket);

        return new EventConfirmBookingResponseDto(savedTicket.getTicketId(), qrCode, toDto(confirmedBooking));
    }

    @Transactional
    public void handlePaymentFailed(String razorpayOrderId) {
        if (razorpayOrderId == null || razorpayOrderId.isBlank()) {
            return;
        }

        eventBookingRepository.findByRazorpayOrderId(razorpayOrderId).ifPresent(booking -> {
            if (booking.getBookingStatus() == EventBooking.BookingStatus.LOCKED) {
                booking.setBookingStatus(EventBooking.BookingStatus.FAILED);
                booking.setLockExpiresAt(null);
                eventBookingRepository.save(booking);
            }
        });
    }

    @Transactional
    public void releaseLock(Long bookingId) {
        if (bookingId == null) {
            throw new RuntimeException("bookingId is required");
        }

        EventBooking booking = eventBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getBookingStatus() == EventBooking.BookingStatus.LOCKED) {
            booking.setBookingStatus(EventBooking.BookingStatus.CANCELLED);
            booking.setLockExpiresAt(null);
            eventBookingRepository.save(booking);
        }
    }

    public List<EventBookingResponseDto> getBookingsByUser(Long userId) {
        cleanupExpiredLocks();
        return eventBookingRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public EventTicketDetailsResponseDto getTicketByBookingReference(String bookingReference) {
        if (bookingReference == null || bookingReference.isBlank()) {
            throw new RuntimeException("bookingReference is required");
        }

        EventTicket ticket = eventTicketRepository.findByBookingBookingReference(bookingReference)
                .orElseThrow(() -> new RuntimeException("Ticket not found for booking reference: " + bookingReference));

        EventBooking booking = ticket.getBooking();
        Event event = booking.getEvent();

        return new EventTicketDetailsResponseDto(
                ticket.getTicketId(),
                booking.getBookingReference(),
                event.getTitle(),
                event.getDate(),
                event.getTime(),
                event.getLocation(),
                booking.getNumberOfTickets(),
                booking.getTotalAmount(),
                booking.getCustomerName(),
                booking.getBookingStatus() == EventBooking.BookingStatus.CONFIRMED ? "PAID" : booking.getBookingStatus().name(),
                ticket.getQrCode()
        );
    }

    @Transactional
    public void cleanupExpiredLocks() {
        List<EventBooking> expiredLocks = eventBookingRepository.findByBookingStatusAndLockExpiresAtBefore(
                EventBooking.BookingStatus.LOCKED,
                LocalDateTime.now()
        );

        if (expiredLocks.isEmpty()) {
            return;
        }

        expiredLocks.forEach(booking -> booking.setBookingStatus(EventBooking.BookingStatus.EXPIRED));
        expiredLocks.forEach(booking -> booking.setLockExpiresAt(null));
        eventBookingRepository.saveAll(expiredLocks);
    }

    private EventBookingResponseDto toDto(EventBooking booking) {
        Event event = booking.getEvent();
        return new EventBookingResponseDto(
                booking.getBookingId(),
                booking.getBookingReference(),
                event.getId(),
                event.getTitle(),
                event.getDate(),
                event.getTime(),
                event.getLocation(),
                booking.getNumberOfTickets(),
                booking.getUnitPrice(),
                booking.getTotalAmount(),
                booking.getBookingStatus().name(),
                event.getImageUrl(),
                booking.getCreatedAt()
        );
    }

    private Event getEventOrThrow(Integer eventId) {
        if (eventId == null) {
            throw new RuntimeException("eventId is required");
        }
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id : " + eventId));
    }

    private void validateTicketCount(Integer ticketCount) {
        if (ticketCount == null || ticketCount <= 0) {
            throw new RuntimeException("ticketCount must be greater than 0");
        }
    }

    private long getRemainingTickets(Integer eventId) {
        Event event = getEventOrThrow(eventId);
        long confirmed = eventBookingRepository.sumTicketsByEventIdAndStatus(eventId, EventBooking.BookingStatus.CONFIRMED);
        long locked = eventBookingRepository.sumActiveLockedTicketsByEventId(eventId, LocalDateTime.now());
        long capacity = event.getTotalTickets() == null ? 0 : event.getTotalTickets();
        long remaining = capacity - confirmed - locked;
        return Math.max(remaining, 0);
    }

    private String buildQrCodeValue(Integer eventId, String bookingReference) {
        return "EVT|" + eventId + "|" + bookingReference;
    }

    private double parsePrice(String priceText) {
        if (priceText == null || priceText.isBlank()) {
            return 0.0;
        }

        String normalized = priceText.toLowerCase(Locale.ROOT).replaceAll(",", "");
        String number = normalized.replaceAll("[^0-9.]", " ").trim();
        if (number.isBlank()) {
            return 0.0;
        }

        String[] tokens = number.split("\\s+");
        return Double.parseDouble(tokens[0]);
    }
}
