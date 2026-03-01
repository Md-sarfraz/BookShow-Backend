package com.jwtAuthentication.jwt.service;

import com.jwtAuthentication.jwt.DTO.responseDto.AdminDashboardResponseDTO;
import com.jwtAuthentication.jwt.repository.EventRepository;
import com.jwtAuthentication.jwt.repository.MovieRepository;
import com.jwtAuthentication.jwt.repository.TheaterRepository;
import com.jwtAuthentication.jwt.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public AdminService(
            MovieRepository movieRepository,
            TheaterRepository theaterRepository,
            UserRepository userRepository,
            EventRepository eventRepository
    ) {
        this.movieRepository = movieRepository;
        this.theaterRepository = theaterRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    public AdminDashboardResponseDTO getDashboardCounts() {

        return new AdminDashboardResponseDTO(
                movieRepository.count(),
                theaterRepository.count(),
                userRepository.count(),
                eventRepository.count()
        );
    }
}
