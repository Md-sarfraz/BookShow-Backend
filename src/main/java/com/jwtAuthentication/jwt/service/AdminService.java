package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.DTO.responseDto.AdminDashboardResponseDTO;
import com.jwtAuthentication.jwt.DTO.responseDto.RevenueChartDataDTO;
import com.jwtAuthentication.jwt.DTO.responseDto.MoviePerformanceDTO;
import com.jwtAuthentication.jwt.DTO.responseDto.TheaterPerformanceDTO;
import com.jwtAuthentication.jwt.DTO.responseDto.SeatOccupancyDTO;
import com.jwtAuthentication.jwt.DTO.responseDto.ReportSummaryDTO;
import com.jwtAuthentication.jwt.model.Booking;
import com.jwtAuthentication.jwt.model.Show;
import com.jwtAuthentication.jwt.model.User;
import com.jwtAuthentication.jwt.repository.BookingRepository;
import com.jwtAuthentication.jwt.repository.EventRepository;
import com.jwtAuthentication.jwt.repository.MovieRepository;
import com.jwtAuthentication.jwt.repository.ShowRepository;
import com.jwtAuthentication.jwt.repository.TheaterRepository;
import com.jwtAuthentication.jwt.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            MovieRepository movieRepository,
            TheaterRepository theaterRepository,
            UserRepository userRepository,
            EventRepository eventRepository,
            BookingRepository bookingRepository,
            ShowRepository showRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.movieRepository = movieRepository;
        this.theaterRepository = theaterRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.showRepository = showRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AdminDashboardResponseDTO getDashboardCounts() {
        // Get start of today for filtering today's bookings
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDate today = LocalDate.now();
        
        // Count confirmed bookings only
        long totalBookings = bookingRepository.countByPaymentStatus(Booking.PaymentStatus.CONFIRMED);
        long todayBookings = bookingRepository.countTodayBookings(Booking.PaymentStatus.CONFIRMED, startOfDay);
        
        // Calculate revenue from confirmed bookings only
        double totalRevenue = bookingRepository.calculateTotalRevenue(Booking.PaymentStatus.CONFIRMED);
        double todayRevenue = bookingRepository.calculateTodayRevenue(Booking.PaymentStatus.CONFIRMED, startOfDay);

        // Calculate today's shows and seats sold
        long todayShows = showRepository.countByShowDate(today);
        long seatsSoldToday = bookingRepository.calculateSeatsSoldToday(Booking.PaymentStatus.CONFIRMED, startOfDay);

        return new AdminDashboardResponseDTO(
                movieRepository.count(),
                theaterRepository.count(),
                userRepository.count(),
                eventRepository.count(),
                totalBookings,
                todayBookings,
                totalRevenue,
                todayRevenue,
                todayShows,
                seatsSoldToday
        );
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking updateBookingStatus(Long bookingId, String status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
        
        Booking.PaymentStatus newStatus;
        try {
            newStatus = Booking.PaymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }
        
        booking.setPaymentStatus(newStatus);
        
        // Set confirmedAt if status is CONFIRMED
        if (newStatus == Booking.PaymentStatus.CONFIRMED && booking.getConfirmedAt() == null) {
            booking.setConfirmedAt(LocalDateTime.now());
        }
        
        return bookingRepository.save(booking);
    }

    public List<RevenueChartDataDTO> getRevenueChartData(String period) {
        List<RevenueChartDataDTO> chartData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        if ("daily".equalsIgnoreCase(period)) {
            // Last 7 days
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
                
                List<Booking> bookings = bookingRepository.findBookingsBetweenDates(
                    Booking.PaymentStatus.CONFIRMED, startOfDay, endOfDay);
                
                double revenue = bookings.stream().mapToDouble(Booking::getTotalAmount).sum();
                int count = bookings.size();
                
                chartData.add(new RevenueChartDataDTO(
                    date.format(formatter),
                    revenue,
                    count
                ));
            }
        } else if ("weekly".equalsIgnoreCase(period)) {
            // Last 8 weeks
            for (int i = 7; i >= 0; i--) {
                LocalDate startDate = LocalDate.now().minusWeeks(i).with(java.time.DayOfWeek.MONDAY);
                LocalDate endDate = startDate.plusDays(6);
                
                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
                
                List<Booking> bookings = bookingRepository.findBookingsBetweenDates(
                    Booking.PaymentStatus.CONFIRMED, startDateTime, endDateTime);
                
                double revenue = bookings.stream().mapToDouble(Booking::getTotalAmount).sum();
                int count = bookings.size();
                
                String label = "Week " + (i == 0 ? "(Current)" : "-" + i);
                
                chartData.add(new RevenueChartDataDTO(
                    label,
                    revenue,
                    count
                ));
            }
        } else if ("monthly".equalsIgnoreCase(period)) {
            // Last 6 months
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
            for (int i = 5; i >= 0; i--) {
                LocalDate month = LocalDate.now().minusMonths(i).withDayOfMonth(1);
                LocalDate nextMonth = month.plusMonths(1);
                
                LocalDateTime startDateTime = month.atStartOfDay();
                LocalDateTime endDateTime = nextMonth.atStartOfDay();
                
                List<Booking> bookings = bookingRepository.findBookingsBetweenDates(
                    Booking.PaymentStatus.CONFIRMED, startDateTime, endDateTime);
                
                double revenue = bookings.stream().mapToDouble(Booking::getTotalAmount).sum();
                int count = bookings.size();
                
                chartData.add(new RevenueChartDataDTO(
                    month.format(formatter),
                    revenue,
                    count
                ));
            }
        }
        
        return chartData;
    }

    // Report Methods
    public ReportSummaryDTO getReportSummary(LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> bookings = bookingRepository.findBookingsBetweenDates(
            Booking.PaymentStatus.CONFIRMED, startDate, endDate);
        
        double totalRevenue = bookings.stream().mapToDouble(Booking::getTotalAmount).sum();
        long totalBookings = bookings.size();
        long totalTicketsSold = bookings.stream().mapToLong(Booking::getNumberOfSeats).sum();
        double averageBookingValue = totalBookings > 0 ? totalRevenue / totalBookings : 0;
        
        return new ReportSummaryDTO(totalRevenue, totalBookings, totalTicketsSold, averageBookingValue);
    }

    public List<MoviePerformanceDTO> getMoviePerformance(LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> bookings = bookingRepository.findBookingsBetweenDates(
            Booking.PaymentStatus.CONFIRMED, startDate, endDate);
        
        Map<Integer, MoviePerformanceDTO> moviePerformanceMap = new HashMap<>();
        
        for (Booking booking : bookings) {
            if (booking.getShow() != null && booking.getShow().getMovie() != null) {
                Integer movieId = booking.getShow().getMovie().getMovieId();
                String movieName = booking.getShow().getMovie().getTitle();
                
                MoviePerformanceDTO dto = moviePerformanceMap.getOrDefault(movieId, 
                    new MoviePerformanceDTO(movieName, movieId, 0L, 0.0));
                
                dto.setTotalTicketsSold(dto.getTotalTicketsSold() + booking.getNumberOfSeats());
                dto.setTotalRevenue(dto.getTotalRevenue() + booking.getTotalAmount());
                
                moviePerformanceMap.put(movieId, dto);
            }
        }
        
        return new ArrayList<>(moviePerformanceMap.values()).stream()
            .sorted((a, b) -> Double.compare(b.getTotalRevenue(), a.getTotalRevenue()))
            .collect(Collectors.toList());
    }

    public List<TheaterPerformanceDTO> getTheaterPerformance(LocalDateTime startDate, LocalDateTime endDate) {
        List<Booking> bookings = bookingRepository.findBookingsBetweenDates(
            Booking.PaymentStatus.CONFIRMED, startDate, endDate);
        
        Map<Integer, TheaterPerformanceDTO> theaterPerformanceMap = new HashMap<>();
        
        for (Booking booking : bookings) {
            if (booking.getShow() != null && booking.getShow().getTheater() != null) {
                Integer theaterId = booking.getShow().getTheater().getId();
                String theaterName = booking.getShow().getTheater().getName();
                
                TheaterPerformanceDTO dto = theaterPerformanceMap.getOrDefault(theaterId, 
                    new TheaterPerformanceDTO(theaterName, theaterId, 0L, 0.0));
                
                dto.setTotalBookings(dto.getTotalBookings() + 1);
                dto.setTotalRevenue(dto.getTotalRevenue() + booking.getTotalAmount());
                
                theaterPerformanceMap.put(theaterId, dto);
            }
        }
        
        return new ArrayList<>(theaterPerformanceMap.values()).stream()
            .sorted((a, b) -> Double.compare(b.getTotalRevenue(), a.getTotalRevenue()))
            .collect(Collectors.toList());
    }

    public List<SeatOccupancyDTO> getSeatOccupancy(LocalDateTime startDate, LocalDateTime endDate) {
        List<Show> shows = showRepository.findAll();
        List<SeatOccupancyDTO> occupancyList = new ArrayList<>();
        
        for (Show show : shows) {
            // Filter shows within date range
            LocalDate showDate = show.getShowDate();
            if (showDate != null) {
                LocalDateTime showDateTime = showDate.atStartOfDay();
                if (showDateTime.isBefore(startDate) || showDateTime.isAfter(endDate)) {
                    continue;
                }
            }
            
            // Get bookings for this show
            List<Booking> showBookings = bookingRepository.findByShowShowId(show.getShowId()).stream()
                .filter(b -> b.getPaymentStatus() == Booking.PaymentStatus.CONFIRMED)
                .filter(b -> b.getConfirmedAt() != null && 
                           !b.getConfirmedAt().isBefore(startDate) && 
                           !b.getConfirmedAt().isAfter(endDate))
                .collect(Collectors.toList());
            
            long seatsSold = showBookings.stream().mapToLong(Booking::getNumberOfSeats).sum();
            Integer totalSeats = show.getTotalSeats();
            double occupancy = totalSeats > 0 ? (seatsSold * 100.0 / totalSeats) : 0;
            
            if (show.getMovie() != null && show.getTheater() != null) {
                occupancyList.add(new SeatOccupancyDTO(
                    show.getMovie().getTitle(),
                    show.getTheater().getName(),
                    show.getShowTime() != null ? show.getShowTime().toString() : "N/A",
                    show.getShowDate() != null ? show.getShowDate().toString() : "N/A",
                    seatsSold,
                    totalSeats,
                    Math.round(occupancy * 100.0) / 100.0
                ));
            }
        }
        
        return occupancyList.stream()
            .sorted((a, b) -> Double.compare(b.getOccupancyPercentage(), a.getOccupancyPercentage()))
            .collect(Collectors.toList());
    }

    public User getUserById(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    public User updateUserProfile(int userId, User updatedUser) {
        User user = getUserById(userId);
        
        if (updatedUser.getFirstName() != null) {
            user.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getLastName() != null) {
            user.setLastName(updatedUser.getLastName());
        }
        if (updatedUser.getEmail() != null) {
            user.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPhoneNo() != null) {
            user.setPhoneNo(updatedUser.getPhoneNo());
        }
        if (updatedUser.getBio() != null) {
            user.setBio(updatedUser.getBio());
        }
        if (updatedUser.getCountry() != null) {
            user.setCountry(updatedUser.getCountry());
        }
        
        return userRepository.save(user);
    }

    public void updatePassword(int userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public User updateUserAvatar(int userId, String imageUrl) {
        User user = getUserById(userId);
        user.setImage(imageUrl);
        return userRepository.save(user);
    }
}
