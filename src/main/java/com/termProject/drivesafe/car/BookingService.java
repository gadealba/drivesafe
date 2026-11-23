package com.termProject.drivesafe.car;

import com.termProject.drivesafe.exceptions.RentalException;
import com.termProject.drivesafe.user.UserModel;
import com.termProject.drivesafe.user.UserRepo;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private static final long MINUTES_PER_DAY = 24 * 60;

    private final CarRepo carRepo;
    private final BookingRepo bookingRepo;
    private final UserRepo userRepo;

    public BookingService(CarRepo carRepo, BookingRepo bookingRepo, UserRepo userRepo) {
        this.carRepo = carRepo;
        this.bookingRepo = bookingRepo;
        this.userRepo = userRepo;
    }

    /**
     * Rents a car for [start, end). The car row is locked first, so two requests for the same
     * car are handled one after the other: the second one sees the first booking and is refused.
     */
    @Transactional
    public Booking book(long carId, long renterId, Instant start, Instant end) {
        if (start == null || end == null) {
            throw RentalException.badRequest("start and end are required.");
        }
        if (!start.isBefore(end)) {
            throw RentalException.badRequest("end must be after start.");
        }
        if (start.isBefore(Instant.now())) {
            throw RentalException.badRequest("A rental can't start in the past.");
        }

        CarListing car = carRepo.findByIdForUpdate(carId)
            .orElseThrow(() -> RentalException.notFound("Car not found."));

        if (!car.isActive()) {
            throw RentalException.conflict("This car is not currently listed for rent.");
        }
        if (car.getOwner().getId() == renterId) {
            throw RentalException.badRequest("You can't rent your own car.");
        }
        if (!car.isAvailableDuring(start, end)) {
            throw RentalException.conflict("The owner has not made this car available for those dates.");
        }
        if (bookingRepo.existsOverlapping(carId, BookingStatus.CONFIRMED, start, end)) {
            throw RentalException.conflict("This car is already booked for part of that period.");
        }

        UserModel renter = userRepo.findById(renterId)
            .orElseThrow(() -> RentalException.notFound("User not found."));

        return bookingRepo.save(new Booking(car, renter, start, end, priceFor(car, start, end)));
    }

    /** Daily price times the number of days, with any part of a day counted as a full day. */
    private static BigDecimal priceFor(CarListing car, Instant start, Instant end) {
        long minutes = Duration.between(start, end).toMinutes();
        long days = Math.max(1, (minutes + MINUTES_PER_DAY - 1) / MINUTES_PER_DAY);
        return car.getPricePerDay().multiply(BigDecimal.valueOf(days));
    }
}
