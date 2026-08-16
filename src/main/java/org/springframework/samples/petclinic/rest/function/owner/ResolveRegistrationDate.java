package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Resolves the effective registration date and rolls it onto a business day, publishing it for
 * later steps. The effective date is the one supplied in the request, or the server's current date
 * when none was supplied. When that date falls on a weekend (Saturday or Sunday) it rolls forward to
 * the following Monday, so the stored {@code registrationDate} is always a business day.
 *
 * <p>Publishing the adjusted date here (before {@link CheckDailyLimit} and {@link BuildOwner}) means
 * every value derived from it stays consistent: the daily create-limit counts owners per adjusted
 * business day, and the membership number's year segment uses the adjusted year.
 */
public class ResolveRegistrationDate {

    public void service(@Val OwnerFieldsDto request, Out<LocalDate> registrationDate) {
        LocalDate effective = request.getRegistrationDate();
        if (effective == null) {
            effective = LocalDate.now();
        }
        // A weekend rolls forward to the next Monday.
        while (effective.getDayOfWeek() == DayOfWeek.SATURDAY
                || effective.getDayOfWeek() == DayOfWeek.SUNDAY) {
            effective = effective.plusDays(1);
        }
        registrationDate.set(effective);
    }
}
