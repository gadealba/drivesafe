package com.termProject.drivesafe.car;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Request and response shapes for the car endpoints. Entities are never sent to the client directly. */
public final class CarDtos {

    private CarDtos() {}

    public record AvailabilityWindowDto(Instant start, Instant end) {}

    /** Everything except year, make, model, mileage, pickupLocation and pricePerDay is optional. */
    public record CreateCarRequest(
        Integer year,
        String make,
        String model,
        Integer mileage,
        String pickupLocation,
        BigDecimal pricePerDay,
        // String color,
        // String description,
        // Integer seats,
        // Transmission transmission,
        List<AvailabilityWindowDto> availability) {}

    /** Any field left out (null) stays unchanged. */
    public record UpdateCarRequest(
        BigDecimal pricePerDay,
        String pickupLocation,
        Integer mileage,
        Boolean active) {}

    public record CarResponse(
        Long id,
        Long ownerId,
        String ownerUsername,
        int year,
        String make,
        String model,
        int mileage,
        String pickupLocation,
        BigDecimal pricePerDay,
        // String color,
        // String description,
        // Integer seats,
        // Transmission transmission,
        boolean active,
        List<AvailabilityWindowDto> availability) {

        public static CarResponse from(CarListingModel car) {
            return new CarResponse(
                car.getId(),
                car.getOwner().getId(),
                car.getOwner().getUsername(),
                car.getYear(),
                car.getMake(),
                car.getModel(),
                car.getMileage(),
                car.getPickupLocation(),
                car.getPricePerDay(),
                // car.getColor(),
                // car.getDescription(),
                // car.getSeats(),
                // car.getTransmission(),
                car.isActive(),
                car.getAvailability().stream()
                    .map(w -> new AvailabilityWindowDto(w.getStartTime(), w.getEndTime()))
                    .toList());
        }
    }

    public record BookingRequest(Instant start, Instant end) {}

    public record BookingResponse(
        Long id,
        Long carId,
        Instant start,
        Instant end,
        BigDecimal totalPrice,
        BookingStatus status) {

        public static BookingResponse from(BookingModel b) {
            return new BookingResponse(
                b.getId(), b.getCar().getId(), b.getStartTime(), b.getEndTime(),
                b.getTotalPrice(), b.getStatus());
        }
    }
}
