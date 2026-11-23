package com.termProject.drivesafe.car;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CarRepo extends JpaRepository<CarListing, Long> {

    List<CarListing> findByActiveTrue();

    List<CarListing> findByOwnerId(Long ownerId);

    /**
     * Loads a car and locks its row until the transaction ends. Bookings and calendar changes
     * for the same car therefore run one at a time, which is what stops two renters from
     * booking the same dates.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CarListing c where c.id = :id")
    Optional<CarListing> findByIdForUpdate(@Param("id") Long id);

    /** Active cars with one window covering [start, end] and no confirmed booking overlapping it. */
    @Query("""
        select distinct c from CarListing c
        join c.availability w
        where c.active = true
          and w.startTime <= :start and w.endTime >= :end
          and not exists (
              select 1 from Booking b
              where b.car = c and b.status = :confirmed
                and b.startTime < :end and b.endTime > :start)
        """)
    List<CarListing> findAvailable(@Param("start") Instant start,
                                   @Param("end") Instant end,
                                   @Param("confirmed") BookingStatus confirmed);
}
