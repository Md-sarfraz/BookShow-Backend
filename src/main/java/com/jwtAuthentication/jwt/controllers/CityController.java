package com.jwtAuthentication.jwt.controllers;

import com.jwtAuthentication.jwt.model.City;
import com.jwtAuthentication.jwt.service.CityService;
import com.jwtAuthentication.jwt.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    // Public: list all cities (used by navbar dropdown)
    @GetMapping
    public ResponseEntity<ApiResponse<List<City>>> getAllCities() {
        List<City> cities = cityService.getAllCities();
        return ResponseEntity.ok(new ApiResponse<>(true, "Cities fetched successfully", cities));
    }

    // Public: get a single city by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<City>> getCityById(@PathVariable Integer id) {
        City city = cityService.getCityById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "City fetched successfully", city));
    }

    // Admin-only: create one city
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<City>> createCity(@RequestBody City city) {
        City created = cityService.createCity(city);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "City created successfully", created));
    }

    // Admin-only: bulk-create cities
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<City>>> createBulkCities(@RequestBody List<City> cities) {
        List<City> saved = cities.stream().map(cityService::createCity).toList();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Cities created successfully", saved));
    }
}
