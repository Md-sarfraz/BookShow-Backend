package com.jwtAuthentication.jwt.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.jwtAuthentication.jwt.DTO.requestDto.EventRequestDto;
import com.jwtAuthentication.jwt.mapper.EventMapper;
import com.jwtAuthentication.jwt.model.Event;
import com.jwtAuthentication.jwt.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {


    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private EventRepository eventRepository;


    // Create Event
    public Event saveEvent(Event event, MultipartFile imageFile, MultipartFile backgroundImageFile) throws IOException, IOException {

        // Upload main image to Cloudinary
        Map uploadResultMain = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
        String imageUrl = uploadResultMain.get("secure_url").toString();
        event.setImageUrl(imageUrl);

        // Upload background image to Cloudinary
        Map uploadResultBg = cloudinary.uploader().upload(backgroundImageFile.getBytes(), ObjectUtils.emptyMap());
        String backgroundImageUrl = uploadResultBg.get("secure_url").toString();
        event.setBackgroundImageUrl(backgroundImageUrl);


        return eventRepository.save(event);
    }


    // Get All Events
    public List<EventRequestDto> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        return events.stream()
                .map(EventMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Get Event By Id
    public EventRequestDto getEventById(int id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id : " + id));
        return EventMapper.toDTO(event);
    }

    // Update Event
    public String updateEvent(int id, EventRequestDto eventRequestDto) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id : " + id));

        event.setTitle(eventRequestDto.getTitle());
        event.setCategory(eventRequestDto.getCategory());
        event.setDate(eventRequestDto.getDate());
        event.setTime(eventRequestDto.getTime());
        event.setLocation(eventRequestDto.getLocation());
        event.setImageUrl(eventRequestDto.getImageUrl());
        event.setBackgroundImageUrl(eventRequestDto.getBackgroundImageUrl());
        event.setPrice(eventRequestDto.getPrice());
        event.setDescription(eventRequestDto.getDescription());

        eventRepository.save(event);
        return "Event Updated Successfully";
    }


    // Delete Event
    public String deleteEvent(int id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id : " + id));
        eventRepository.delete(event);
        return "Event Deleted Successfully";
    }
}
