package com.termProject.drivesafe.car;

import com.termProject.drivesafe.exceptions.RentalException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** A stretch of time, from start up to (not including) end. */
public record TimeRange(Instant start, Instant end) {

    /**
     * Checks every range, sorts them, and merges ranges that overlap or touch,
     * so a booking that spans two neighbouring windows is still recognised as available.
     */
    public static List<TimeRange> normalize(List<TimeRange> ranges) {
        if (ranges == null || ranges.isEmpty()) return List.of();

        List<TimeRange> sorted = new ArrayList<>();
        for (TimeRange range : ranges) {
            if (range == null || range.start() == null || range.end() == null) {
                throw RentalException.badRequest("Each availability window needs a start and an end.");
            }
            if (!range.start().isBefore(range.end())) {
                throw RentalException.badRequest("An availability window must end after it starts.");
            }
            sorted.add(range);
        }
        sorted.sort(Comparator.comparing(TimeRange::start));

        List<TimeRange> merged = new ArrayList<>();
        TimeRange current = sorted.get(0);
        for (int i = 1; i < sorted.size(); i++) {
            TimeRange next = sorted.get(i);
            if (!next.start().isAfter(current.end())) {
                if (next.end().isAfter(current.end())) {
                    current = new TimeRange(current.start(), next.end());
                }
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        return merged;
    }
}
