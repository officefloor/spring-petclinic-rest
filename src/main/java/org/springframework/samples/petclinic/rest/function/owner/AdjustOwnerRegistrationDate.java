package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Ensures the effective registration date falls on a business day: weekends and listed public
 * holidays roll forward to the next non-holiday weekday. Runs after {@code BuildOwner} has resolved
 * the date (supplied in the request or defaulted to today) and before any step reads it, so every
 * value derived from it — the membership number's year segment and the daily create-limit count —
 * uses the adjusted date.
 */
public class AdjustOwnerRegistrationDate {

    /** Fixed public holidays the business-day roll skips. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
        LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
        LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"), LocalDate.parse("2026-12-28"));

    public void service(@Val Owner owner) {
        LocalDate date = owner.getRegistrationDate();
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        owner.setRegistrationDate(date);
    }
}
