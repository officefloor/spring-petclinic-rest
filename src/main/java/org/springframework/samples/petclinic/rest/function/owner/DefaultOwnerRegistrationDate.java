package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Defaults and business-day-adjusts the owner {@code registrationDate} on create. An absent (null)
 * value is first set to the server's current date; then the effective date — supplied or defaulted —
 * must fall on a business day: a Saturday, Sunday or listed public holiday is rolled forward to the
 * next non-holiday weekday. The
 * adjusted value is written back onto the (shared) {@link OwnerFieldsDto}, so every later step sees it:
 * {@link RequireDailyOwnerLimit} counts per adjusted business day, {@link BuildOwner} stores it, the
 * membership number's year segment derives from it, and {@code GET /owners/{id}} returns it in ISO
 * 'YYYY-MM-DD' format. Only wired into the create pipeline, so updates never re-stamp the date.
 */
public class DefaultOwnerRegistrationDate {

    public void service(@Val OwnerFieldsDto owner) {
        LocalDate date = owner.getRegistrationDate();
        if (date == null) {
            date = LocalDate.now();
        }
        owner.setRegistrationDate(toBusinessDay(date));
    }

    /** Fixed public holidays the business-day roll skips. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"), LocalDate.parse("2026-12-28"));

    private static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
}
