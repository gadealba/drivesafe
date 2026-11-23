package com.termProject.drivesafe.car;

import com.termProject.drivesafe.car.CarDtos.AvailabilityWindowDto;
import com.termProject.drivesafe.car.CarDtos.CreateCarRequest;
import com.termProject.drivesafe.car.CarDtos.UpdateCarRequest;
import com.termProject.drivesafe.exceptions.RentalException;
import com.termProject.drivesafe.user.UserModel;
import com.termProject.drivesafe.user.UserRepo;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarService {

    private final CarRepo carRepo;
    private final BookingRepo bookingRepo;
    private final UserRepo userRepo;

    public CarService(CarRepo carRepo, BookingRepo bookingRepo, UserRepo userRepo) {
        this.carRepo = carRepo;
        this.bookingRepo = bookingRepo;
        this.userRepo = userRepo;
    }

    /** Lists a car. This method hands the request's details to the Builder and saves the result. */
    @Transactional
    public CarListing create(long ownerId, CreateCarRequest request) {
        UserModel owner = userRepo.findById(ownerId)
            .orElseThrow(() -> RentalException.notFound("User not found."));

        CarListing.Builder builder = CarListing.builder()
            .owner(owner)
            .year(request.year())
            .make(request.make())
            .model(request.model())
            .mileage(request.mileage())
            .pickupLocation(request.pickupLocation())
            .pricePerDay(request.pricePerDay())
            .color(request.color())
            .description(request.description())
            .seats(request.seats())
            .transmission(request.transmission());

        if (request.availability() != null) {
            for (AvailabilityWindowDto window : request.availability()) {
                builder.availableFrom(window.start(), window.end());
            }
        }
        return carRepo.save(builder.build());
    }

    /**
     * Cars renters can see. With no dates, every active listing. With start and end, only
     * cars that are free for that whole period.
     */
    @Transactional(readOnly = true)
    public List<CarListing> browse(Instant start, Instant end) {
        List<CarListing> cars;
        if (start == null && end == null) {
            cars = carRepo.findByActiveTrue();
        } else if (start == null || end == null) {
            throw RentalException.badRequest("Send both start and end, or neither.");
        } else if (!start.isBefore(end)) {
            throw RentalException.badRequest("end must be after start.");
        } else {
            cars = carRepo.findAvailable(start, end, BookingStatus.CONFIRMED);
        }
        return cars.stream().sorted(Comparator.comparing(CarListing::getPricePerDay)).toList();
    }

    @Transactional(readOnly = true)
    public List<CarListing> listOwnedBy(long ownerId) {
        return carRepo.findByOwnerId(ownerId);
    }

    /** One listing. An inactive one is only visible to its owner. */
    @Transactional(readOnly = true)
    public CarListing get(long carId, long viewerId) {
        CarListing car = carRepo.findById(carId)
            .orElseThrow(() -> RentalException.notFound("Car not found."));
        if (!car.isActive() && car.getOwner().getId() != viewerId) {
            throw RentalException.notFound("Car not found.");
        }
        return car;
    }

    /** Owner changes price, pickup location, mileage, or turns the listing on or off. */
    @Transactional
    public CarListing update(long carId, long ownerId, UpdateCarRequest request) {
        CarListing car = findOwned(carId, ownerId);
        if (request.pricePerDay() != null) car.changePrice(request.pricePerDay());
        if (request.pickupLocation() != null) car.changePickupLocation(request.pickupLocation());
        if (request.mileage() != null) car.changeMileage(request.mileage());
        if (request.active() != null) car.setActive(request.active());
        return car;
    }

    /**
     * Owner replaces the calendar. Upcoming bookings must still fit inside the new windows,
     * otherwise the change is refused. The car row is locked so nobody can book mid-change.
     */
    @Transactional
    public CarListing updateAvailability(long carId, long ownerId, List<AvailabilityWindowDto> windows) {
        CarListing car = carRepo.findByIdForUpdate(carId)
            .orElseThrow(() -> RentalException.notFound("Car not found."));
        requireOwner(car, ownerId);

        List<TimeRange> ranges = TimeRange.normalize(
            windows == null ? List.of() : windows.stream()
                .map(w -> new TimeRange(w.start(), w.end()))
                .toList());

        List<Booking> upcoming = bookingRepo.findByCarIdAndStatusAndEndTimeAfter(
            carId, BookingStatus.CONFIRMED, Instant.now());
        for (Booking booking : upcoming) {
            boolean stillCovered = ranges.stream().anyMatch(r ->
                !r.start().isAfter(booking.getStartTime()) && !r.end().isBefore(booking.getEndTime()));
            if (!stillCovered) {
                throw RentalException.conflict(
                    "A booking from " + booking.getStartTime() + " to " + booking.getEndTime()
                        + " would fall outside the new availability.");
            }
        }

        car.replaceAvailability(ranges);
        return car;
    }

    // ---- helpers ------------------------------------------------------------

    private CarListing findOwned(long carId, long ownerId) {
        CarListing car = carRepo.findById(carId)
            .orElseThrow(() -> RentalException.notFound("Car not found."));
        requireOwner(car, ownerId);
        return car;
    }

    private static void requireOwner(CarListing car, long userId) {
        if (car.getOwner().getId() != userId) {
            throw RentalException.forbidden("You can only manage your own listings.");
        }
    }
}
