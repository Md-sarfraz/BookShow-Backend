package com.jwtAuthentication.jwt.config;

import com.jwtAuthentication.jwt.model.City;
import com.jwtAuthentication.jwt.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Runs once on every startup:
 * 1. Seeds default Indian cities into the city table (if it is empty).
 * 2. Back-fills theater.city_id for any theater that still has the legacy
 *    theater.city varchar column set but no city_id yet.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CityRepository cityRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final List<String> DEFAULT_CITIES = List.of(
        "Delhi", "Mumbai", "Bengaluru", "Chennai", "Kolkata",
        "Hyderabad", "Chandigarh", "Noida", "Pune", "Ahmedabad",
        "Jaipur", "Surat", "Lucknow", "Kochi", "Indore"
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedDefaultCities();
        backfillTheaterCityId();
    }

    /** Insert default cities if the table is completely empty. */
    private void seedDefaultCities() {
        if (cityRepository.count() > 0) {
            log.info("[DataInitializer] City table already populated ({} rows) – skipping seed.",
                cityRepository.count());
            return;
        }
        for (String name : DEFAULT_CITIES) {
            City city = new City();
            city.setName(name);
            cityRepository.save(city);
        }
        log.info("[DataInitializer] Seeded {} default cities.", DEFAULT_CITIES.size());
    }

    /**
     * For theaters that still carry the old string city name but no city_id FK,
     * find (or create) the matching City row and set city_id.
     */
    private void backfillTheaterCityId() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT id, city FROM theater WHERE city_id IS NULL AND city IS NOT NULL AND TRIM(city) != ''"
        );

        if (rows.isEmpty()) {
            log.info("[DataInitializer] No theaters need city_id back-fill.");
            return;
        }

        log.info("[DataInitializer] Back-filling city_id for {} theater(s)...", rows.size());

        for (Map<String, Object> row : rows) {
            long theaterId = ((Number) row.get("id")).longValue();
            String cityName = ((String) row.get("city")).trim();

            City city = cityRepository.findByNameIgnoreCase(cityName)
                .orElseGet(() -> {
                    City c = new City();
                    c.setName(cityName);
                    City saved = cityRepository.save(c);
                    log.info("[DataInitializer] Created city '{}' (id={}) for theater {}",
                        cityName, saved.getId(), theaterId);
                    return saved;
                });

            jdbcTemplate.update("UPDATE theater SET city_id = ? WHERE id = ?",
                city.getId(), theaterId);
            log.info("[DataInitializer] Theater {} => '{}' (city_id={})",
                theaterId, city.getName(), city.getId());
        }

        log.info("[DataInitializer] Back-fill complete.");
    }
}
