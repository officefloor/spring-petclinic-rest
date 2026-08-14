package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Resolves a create-owner request's effective registration date and forces it onto a business day.
 * When the client supplied no date, the server's current date is used; a supplied date is kept.
 * Either way, if the effective date falls on a Saturday or Sunday it is rolled forward to the next
 * Monday, so the persisted {@code registrationDate} is always a business day.
 *
 * <p>Runs after {@link BuildOwner} has mapped the request onto the entity and mutates that same
 * entity in place (via {@code @Val}), so {@link CheckDailyLimit}, {@link SaveOwner} and any value
 * derived from the registration date (e.g. the membership number's year segment) all see the
 * adjusted date.
 */
public class DefaultRegistrationDate {

    public void service(@Val Owner owner) {
        LocalDate date = owner.getRegistrationDate();
        if (date == null) {
            date = LocalDate.now();
        }
        owner.setRegistrationDate(toBusinessDay(date));
    }

    /** Roll a weekend date forward to the next Monday; a weekday is returned unchanged. */
    private static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
