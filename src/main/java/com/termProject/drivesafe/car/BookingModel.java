package com.termProject.drivesafe.car;

import com.termProject.drivesafe.user.UserModel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** One renter's reservation of one car for a period of time. */
@Entity
@Table(
    name = "bookings",
    indexes = @Index(name = "idx_bookings_car_time", columnList = "car_id, start_time, end_time")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private CarListing car;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "renter_id", nullable = false)
    private UserModel renter;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    /** Price at the time of booking, so later price changes don't affect it. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Booking(CarListing car, UserModel renter, Instant startTime, Instant endTime, BigDecimal totalPrice) {
        this.car = car;
        this.renter = renter;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalPrice = totalPrice;
    }
}
