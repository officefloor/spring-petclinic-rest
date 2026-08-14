package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Shared business-day adjustment for the create-owner pipeline. The effective registration date
 * must fall on a business day: when it lands on a Saturday or Sunday it rolls forward to the next
 * Monday. Weekdays are kept unchanged and the transform is idempotent.
 *
 * <p>Used for the single effective registration date so every value derived from it — what is
 * stored and returned, the membership number's year segment and the daily create-limit's per-day
 * bucket — agrees on the same adjusted date.
 */
public final class BusinessDays {

    private BusinessDays() {
    }

    /** Roll {@code date} forward to the next Monday when it falls on a weekend; otherwise keep it. */
    public static LocalDate adjust(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (dow == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }
}
