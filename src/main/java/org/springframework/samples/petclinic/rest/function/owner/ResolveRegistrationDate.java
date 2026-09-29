package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Resolves the effective registration date and rolls it onto a business day. The effective
 * date is the request-supplied {@code registrationDate} when present, otherwise the
 * server's current date; when that date falls on a Saturday or Sunday it rolls forward to
 * the following Monday. The resolved business-day date is published so the steps that count
 * owners per day, stamp the owner and derive the membership number all use the same
 * adjusted value.
 */
public class ResolveRegistrationDate {

    public void service(@Val OwnerFieldsDto request, Out<LocalDate> registrationDate) {
        LocalDate effective = request.getRegistrationDate();
        if (effective == null) {
            effective = LocalDate.now();
        }
        registrationDate.set(toBusinessDay(effective));
    }

    /** Rolls a weekend date forward to the following Monday; a business day is unchanged. */
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
