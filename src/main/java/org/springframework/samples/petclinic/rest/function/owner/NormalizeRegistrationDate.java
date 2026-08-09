package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Resolves the EFFECTIVE registration date and rolls it onto a business day.
 *
 * <p>The effective date is the one supplied in the request, or the server's current date when the
 * request omits it. Either way, when that date lands on a Saturday or Sunday it is rolled forward to
 * the next Monday. The adjusted date is published as a variable so every later step derives from the
 * same value: the daily create-limit counts owners per adjusted business day, {@link BuildOwner}
 * stores it as the owner's {@code registrationDate}, and {@link AssignMembershipNumber} takes the
 * membership number's year segment from it.
 *
 * <p>Runs before {@link EnsureDailyOwnerLimit} so the limit is checked against the adjusted date.
 */
public class NormalizeRegistrationDate {

    public void service(@Val OwnerFieldsDto request, Out<LocalDate> registrationDate) {
        LocalDate effective = request.getRegistrationDate() != null
                ? request.getRegistrationDate() : LocalDate.now();
        registrationDate.set(toBusinessDay(effective));
    }

    private static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
