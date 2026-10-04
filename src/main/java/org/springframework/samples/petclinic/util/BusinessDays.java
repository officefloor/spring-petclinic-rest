package org.springframework.samples.petclinic.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Rolls a calendar date forward onto a business day.
 *
 * <p>A business day is any day that is not a Saturday or Sunday. When the supplied date already
 * falls on a weekday it is returned unchanged; when it falls on a Saturday or Sunday it is rolled
 * forward to the following Monday. The operation is idempotent: feeding it an already-adjusted
 * (weekday) value returns that same value.
 */
public final class BusinessDays {

    private BusinessDays() {
    }

    /**
     * Return {@code date} when it falls on a weekday, or the next Monday when it falls on a
     * Saturday or Sunday. Returns {@code null} when the input is {@code null}.
     */
    public static LocalDate rollToBusinessDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (day == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }

    /**
     * Resolve the effective registration date: the supplied date, or the server's current date
     * when {@code null}, rolled forward onto a business day. This is the single definition of the
     * "default-to-now, then roll" policy shared by the create controller (which needs the value
     * eagerly) and the {@code @PrePersist} backstop. Idempotent for an already-adjusted value.
     */
    public static LocalDate effectiveRegistrationDate(LocalDate supplied) {
        return rollToBusinessDay(supplied != null ? supplied : LocalDate.now());
    }
}
