package com.termProject.drivesafe.car;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepo extends JpaRepository<Booking, Long> {

    /**
     * Two periods overlap when each one starts before the other ends. Using "<" and ">"
     * (not "<=") lets one rental start the moment the previous one ends.
     */
    @Query("""
        select count(b) > 0 from Booking b
        where b.car.id = :carId and b.status = :status
          and b.startTime < :end and b.endTime > :start
        """)
    boolean existsOverlapping(@Param("carId") Long carId,
                              @Param("status") BookingStatus status,
                              @Param("start") Instant start,
                              @Param("end") Instant end);

    /** Bookings of a car that have not finished yet. */
    List<Booking> findByCarIdAndStatusAndEndTimeAfter(Long carId, BookingStatus status, Instant after);
}
