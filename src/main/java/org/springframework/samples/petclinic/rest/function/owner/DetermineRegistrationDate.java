package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Step of {@code POST /api/owners} that fixes the EFFECTIVE registration date for the create —
 * the date supplied on the request, or the server's current date when none is supplied — and rolls
 * a weekend or public-holiday date forward to the next non-holiday business day so the registration
 * date always falls on a working business day.
 *
 * <p>Published as a variable so every later step works from the one adjusted date: the daily
 * create-limit counts owners per this business day, {@link BuildOwner} stores it, and any value
 * derived from it (such as the membership number's year segment) follows.
 */
public class DetermineRegistrationDate {

    public void service(@Val OwnerFieldsDto request, Out<LocalDate> registrationDate) {
        LocalDate date = request.getRegistrationDate();
        if (date == null) {
            date = LocalDate.now();
        }
        registrationDate.set(toBusinessDay(date));
    }

    /** The fixed public-holiday calendar the registration date rolls past. */
    static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 26),
            LocalDate.of(2026, 4, 25),
            LocalDate.of(2026, 12, 25),
            LocalDate.of(2026, 12, 28));

    /**
     * Roll forward to the next non-holiday business day: a Saturday, Sunday or listed public holiday
     * advances a day at a time until it lands on a weekday that is not a holiday. A working business
     * day is unchanged.
     */
    static LocalDate toBusinessDay(LocalDate date) {
        while (isWeekend(date) || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
