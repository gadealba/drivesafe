package com.termProject.drivesafe.car;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.termProject.drivesafe.exceptions.RentalException;
import com.termProject.drivesafe.user.UserModel;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class CarListingBuilderTest {

    private static final Instant T9 = Instant.parse("2030-01-01T09:00:00Z");
    private static final Instant T12 = Instant.parse("2030-01-01T12:00:00Z");
    private static final Instant T15 = Instant.parse("2030-01-01T15:00:00Z");
    private static final Instant T18 = Instant.parse("2030-01-01T18:00:00Z");

    private final UserModel owner = new UserModel("owner", "hash", "owner@example.com");

    /** A builder with every required detail filled in. */
    private CarListing.Builder validBuilder() {
        return CarListing.builder()
            .owner(owner)
            .year(2020)
            .make("Toyota")
            .model("Corolla")
            .mileage(42000)
            .pickupLocation("Dearborn, MI")
            .pricePerDay(new BigDecimal("45"));
    }

    @Test
    void buildsWithOnlyTheRequiredDetails() {
        CarListing car = validBuilder().build();

        assertEquals("Toyota", car.getMake());
        assertEquals(new BigDecimal("45.00"), car.getPricePerDay());
        assertTrue(car.isActive());
        assertNull(car.getColor());
        assertNull(car.getSeats());
        assertNull(car.getTransmission());
        assertTrue(car.getAvailability().isEmpty());
    }

    @Test
    void optionalDetailsAreKeptWhenProvided() {
        CarListing car = validBuilder()
            .color("  Blue ")
            .seats(5)
            .transmission(Transmission.AUTOMATIC)
            .description("Clean and efficient")
            .build();

        assertEquals("Blue", car.getColor());
        assertEquals(5, car.getSeats());
        assertEquals(Transmission.AUTOMATIC, car.getTransmission());
        assertEquals("Clean and efficient", car.getDescription());
    }

    @Test
    void missingRequiredDetailsAreRejected() {
        assertThrows(RentalException.class, () -> CarListing.builder().build());
        assertThrows(RentalException.class, () -> validBuilder().make(" ").build());
        assertThrows(RentalException.class, () -> validBuilder().year(null).build());
        assertThrows(RentalException.class, () -> validBuilder().pricePerDay(null).build());
    }

    @Test
    void invalidValuesAreRejected() {
        assertThrows(RentalException.class, () -> validBuilder().year(1800).build());
        assertThrows(RentalException.class, () -> validBuilder().mileage(-1).build());
        assertThrows(RentalException.class, () -> validBuilder().pricePerDay(BigDecimal.ZERO).build());
        assertThrows(RentalException.class, () -> validBuilder().seats(0).build());
    }

    @Test
    void availabilityWindowsAreAddedAndOverlappingOnesMerged() {
        CarListing car = validBuilder()
            .availableFrom(T9, T12)
            .availableFrom(T12, T15)   // touches the first one
            .availableFrom(T18, T18.plusSeconds(3600))
            .build();

        List<AvailabilityWindow> windows = car.getAvailability();
        assertEquals(2, windows.size());
        assertEquals(T9, windows.get(0).getStartTime());
        assertEquals(T15, windows.get(0).getEndTime());
    }

    @Test
    void aWindowThatEndsBeforeItStartsIsRejected() {
        assertThrows(RentalException.class, () -> validBuilder().availableFrom(T12, T9).build());
    }

    @Test
    void isAvailableDuringNeedsOneWindowCoveringTheWholePeriod() {
        CarListing car = validBuilder().availableFrom(T9, T15).build();

        assertTrue(car.isAvailableDuring(T9, T12));
        assertTrue(car.isAvailableDuring(T9, T15));
        assertFalse(car.isAvailableDuring(T12, T18));
    }

    @Test
    void ownerChangesAreValidated() {
        CarListing car = validBuilder().build();

        car.changePrice(new BigDecimal("60.5"));
        assertEquals(new BigDecimal("60.50"), car.getPricePerDay());
        assertThrows(RentalException.class, () -> car.changePrice(new BigDecimal("-5")));
    }
}
