package com.termProject.drivesafe.car;

import com.termProject.drivesafe.exceptions.RentalException;
import com.termProject.drivesafe.user.UserModel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * A car an owner has listed for short-term rental.
 *
 * Builder pattern: the constructor is private, so a listing can only be made through
 * CarListing.builder()...build(). Required details are checked in build(); the rest are
 * optional and can be left out.
 */
@Entity
@Table(name = "car_listings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CarListingModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserModel owner;

    @Column(name = "model_year", nullable = false)
    private int year;

    @Column(nullable = false)
    private String make;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private int mileage;

    @Column(nullable = false)
    private String pickupLocation;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    // // ---- optional details ----
    // private String color;

    // @Column(length = 1000)
    // private String description;

    // private Integer seats;

    // @Enumerated(EnumType.STRING)
    // private Transmission transmission;

    /** An inactive listing is hidden from renters and cannot be booked. */
    @Column(nullable = false)
    private boolean active = true;

    /** The calendar: when this car can be rented. */
    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startTime ASC")
    private List<AvailabilityWindowModel> availability = new ArrayList<>();

    // =====================================================================
    // Builder
    // =====================================================================

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UserModel owner;
        private Integer year;
        private String make;
        private String model;
        private Integer mileage;
        private String pickupLocation;
        private BigDecimal pricePerDay;
        // private String color;
        // private String description;
        // private Integer seats;
        // private Transmission transmission;
        private final List<TimeRange> windows = new ArrayList<>();

        private Builder() {}

        public Builder owner(UserModel owner) { this.owner = owner; return this; }
        public Builder year(Integer year) { this.year = year; return this; }
        public Builder make(String make) { this.make = make; return this; }
        public Builder model(String model) { this.model = model; return this; }
        public Builder mileage(Integer mileage) { this.mileage = mileage; return this; }
        public Builder pickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; return this; }
        public Builder pricePerDay(BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; return this; }


        // public Builder color(String color) { this.color = color; return this; }
        // public Builder description(String description) { this.description = description; return this; }
        // public Builder seats(Integer seats) { this.seats = seats; return this; }
        // public Builder transmission(Transmission transmission) { this.transmission = transmission; return this; }

        /** Adds a window of time when the car can be rented. Call it as many times as you like. */
        public Builder availableFrom(Instant start, Instant end) {
            this.windows.add(new TimeRange(start, end));
            return this;
        }

        /** Validates everything and creates the listing. Throws a 400 RentalException if something is wrong. */
        public CarListingModel build() {
            return new CarListingModel(this);
        }
    }

    private CarListingModel(Builder b) {
        if (b.owner == null) throw RentalException.badRequest("A listing needs an owner.");
        this.owner = b.owner;
        this.year = requireYear(b.year);
        this.make = requireText(b.make, "make");
        this.model = requireText(b.model, "model");
        this.mileage = requireMileage(b.mileage);
        this.pickupLocation = requireText(b.pickupLocation, "pickupLocation");
        this.pricePerDay = requirePrice(b.pricePerDay);

        // this.color = blankToNull(b.color);
        // this.description = blankToNull(b.description);
        // this.seats = checkSeats(b.seats);
        // this.transmission = b.transmission;

        replaceAvailability(b.windows);
    }

    // =====================================================================
    // Changes an owner is allowed to make later
    // =====================================================================

    public void changePrice(BigDecimal newPrice) {
        this.pricePerDay = requirePrice(newPrice);
    }

    public void changePickupLocation(String newLocation) {
        this.pickupLocation = requireText(newLocation, "pickupLocation");
    }

    public void changeMileage(Integer newMileage) {
        this.mileage = requireMileage(newMileage);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /** Replaces the whole calendar. Overlapping or touching windows are merged. */
    public void replaceAvailability(List<TimeRange> ranges) {
        List<TimeRange> normalized = TimeRange.normalize(ranges);
        // Change the existing list in place so Hibernate can delete the removed windows.
        this.availability.clear();
        for (TimeRange range : normalized) {
            this.availability.add(new AvailabilityWindowModel(this, range.start(), range.end()));
        }
    }

    /** True when one availability window covers the whole requested period. */
    public boolean isAvailableDuring(Instant start, Instant end) {
        return availability.stream().anyMatch(w ->
            !w.getStartTime().isAfter(start) && !w.getEndTime().isBefore(end));
    }

    // =====================================================================
    // Validation helpers
    // =====================================================================

    private static int requireYear(Integer year) {
        if (year == null) throw RentalException.badRequest("year is required.");
        int latest = Year.now().getValue() + 1;
        if (year < 1950 || year > latest) {
            throw RentalException.badRequest("year must be between 1950 and " + latest + ".");
        }
        return year;
    }

    private static int requireMileage(Integer mileage) {
        if (mileage == null) throw RentalException.badRequest("mileage is required.");
        if (mileage < 0) throw RentalException.badRequest("mileage cannot be negative.");
        return mileage;
    }

    private static BigDecimal requirePrice(BigDecimal price) {
        if (price == null) throw RentalException.badRequest("pricePerDay is required.");
        if (price.signum() <= 0) throw RentalException.badRequest("pricePerDay must be greater than 0.");
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw RentalException.badRequest(field + " is required.");
        return value.trim();
    }

    // private static Integer checkSeats(Integer seats) {
    //     if (seats != null && (seats < 1 || seats > 20)) {
    //         throw RentalException.badRequest("seats must be between 1 and 20.");
    //     }
    //     return seats;
    // }

    // private static String blankToNull(String value) {
    //     return value == null || value.isBlank() ? null : value.trim();
    // }
}
