package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Step of {@code POST /api/owners} that fixes the EFFECTIVE registration date for the create —
 * the date supplied on the request, or the server's current date when none is supplied — and rolls
 * a weekend date forward to the next Monday so the registration date always falls on a business day.
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

    /** Roll a Saturday or Sunday forward to the next Monday; a business day is unchanged. */
    static LocalDate toBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (day == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }
}
