package com.termProject.drivesafe.car;

import com.termProject.drivesafe.car.CarDtos.BookingRequest;
import com.termProject.drivesafe.car.CarDtos.BookingResponse;
import com.termProject.drivesafe.session.SessionManager.UserSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cars/{carId}/bookings")
public class BookingController {

    private final BookingService bookings;

    public BookingController(BookingService bookings) {
        this.bookings = bookings;
    }

    /** The logged-in user rents the car for the requested period. */
    @PostMapping
    ResponseEntity<BookingResponse> book(@AuthenticationPrincipal UserSession session,
                                         @PathVariable long carId,
                                         @RequestBody BookingRequest request) {
        BookingModel booking = bookings.book(carId, session.userId(), request.start(), request.end());
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }
}
