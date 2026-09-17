package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * The single definition of a "business day" for owner registration: Monday–Friday. A date
 * that falls on a Saturday or Sunday is rolled forward to the following Monday; a weekday is
 * returned unchanged. Used so every effective registration date — whether supplied in the
 * request or defaulted to the server date — lands on a business day, and so the daily
 * create-limit buckets owners by that same adjusted day.
 */
final class BusinessDay {

    private BusinessDay() {
    }

    /** {@code date} rolled forward to the next Monday when it falls on a weekend, else unchanged. */
    static LocalDate adjust(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case SATURDAY:
                return date.plusDays(2);
            case SUNDAY:
                return date.plusDays(1);
            default:
                return date;
        }
    }
}
