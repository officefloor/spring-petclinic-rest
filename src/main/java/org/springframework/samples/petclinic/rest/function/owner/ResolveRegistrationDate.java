package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Resolves the effective registration date and rolls it onto a business day. The effective
 * date is the request-supplied {@code registrationDate} when present, otherwise the
 * server's current date; when that date falls on a Saturday, Sunday or a listed public
 * holiday it rolls forward to the next non-holiday business day. The resolved business-day
 * date is published so the steps that count owners per day, stamp the owner and derive the
 * membership number all use the same adjusted value.
 */
public class ResolveRegistrationDate {

    /** Fixed public holidays the business-day roll skips. */
    static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"), LocalDate.parse("2026-12-28"));

    public void service(@Val OwnerFieldsDto request, Out<LocalDate> registrationDate) {
        LocalDate effective = request.getRegistrationDate();
        if (effective == null) {
            effective = LocalDate.now();
        }
        registrationDate.set(toBusinessDay(effective));
    }

    /**
     * Rolls a date forward over weekends and listed public holidays to the next business
     * day; a business day that is not a holiday is unchanged.
     */
    static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
}
