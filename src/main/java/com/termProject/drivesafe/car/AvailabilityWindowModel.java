package com.termProject.drivesafe.car;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

/** One stretch of time when a car can be rented. A listing's windows make up its calendar. */
@Entity
@Table(name = "availability_windows")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AvailabilityWindow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private CarListing car;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    AvailabilityWindow(CarListing car, Instant startTime, Instant endTime) {
        this.car = car;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
