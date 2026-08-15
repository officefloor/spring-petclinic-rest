package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Resolves a create-owner request's effective registration date and forces it onto a business day.
 * When the client supplied no date, the server's current date is used; a supplied date is kept.
 * Either way, if the effective date falls on a Saturday, Sunday or a listed public holiday it is
 * rolled forward to the next non-holiday weekday, so the persisted {@code registrationDate} is
 * always a business day.
 *
 * <p>Runs after {@link BuildOwner} has mapped the request onto the entity and mutates that same
 * entity in place (via {@code @Val}), so {@link CheckDailyLimit}, {@link SaveOwner} and any value
 * derived from the registration date (e.g. the membership number's year segment) all see the
 * adjusted date.
 */
public class DefaultRegistrationDate {

    /** Fixed public holidays the business-day roll skips. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"), LocalDate.parse("2026-12-28"));

    public void service(@Val Owner owner) {
        LocalDate date = owner.getRegistrationDate();
        if (date == null) {
            date = LocalDate.now();
        }
        owner.setRegistrationDate(toBusinessDay(date));
    }

    /**
     * Roll a date forward to the next business day: weekends and listed public holidays are skipped;
     * a non-holiday weekday is returned unchanged.
     */
    private static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
}
