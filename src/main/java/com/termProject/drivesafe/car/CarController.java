package com.termProject.drivesafe.car;

import com.termProject.drivesafe.car.CarDtos.AvailabilityWindowDto;
import com.termProject.drivesafe.car.CarDtos.CarResponse;
import com.termProject.drivesafe.car.CarDtos.CreateCarRequest;
import com.termProject.drivesafe.car.CarDtos.UpdateCarRequest;
import com.termProject.drivesafe.session.SessionManager.UserSession;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarService cars;

    public CarController(CarService cars) {
        this.cars = cars;
    }

    /** An owner lists a car. The owner is whoever is logged in. */
    @PostMapping
    ResponseEntity<CarResponse> create(@AuthenticationPrincipal UserSession session,
                                       @RequestBody CreateCarRequest request) {
        CarListingModel car = cars.create(session.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CarResponse.from(car));
    }

    /** Cars available for rent. Add ?start=...&end=... (ISO-8601, e.g. 2026-12-01T10:00:00Z) to filter by dates. */
    @GetMapping
    List<CarResponse> browse(@RequestParam(required = false) Instant start,
                             @RequestParam(required = false) Instant end) {
        return cars.browse(start, end).stream().map(CarResponse::from).toList();
    }

    /** The logged-in owner's own listings, including inactive ones. */
    @GetMapping("/mine")
    List<CarResponse> mine(@AuthenticationPrincipal UserSession session) {
        return cars.listOwnedBy(session.userId()).stream().map(CarResponse::from).toList();
    }

    @GetMapping("/{id}")
    CarResponse get(@AuthenticationPrincipal UserSession session, @PathVariable long id) {
        return CarResponse.from(cars.get(id, session.userId()));
    }

    /** Owner updates price, pickup location, mileage, or turns the listing on or off. */
    @PatchMapping("/{id}")
    CarResponse update(@AuthenticationPrincipal UserSession session,
                       @PathVariable long id,
                       @RequestBody UpdateCarRequest request) {
        return CarResponse.from(cars.update(id, session.userId(), request));
    }

    /** Owner replaces the car's calendar with the windows in the request body. */
    @PutMapping("/{id}/availability")
    CarResponse updateAvailability(@AuthenticationPrincipal UserSession session,
                                   @PathVariable long id,
                                   @RequestBody List<AvailabilityWindowDto> windows) {
        return CarResponse.from(cars.updateAvailability(id, session.userId(), windows));
    }
}
