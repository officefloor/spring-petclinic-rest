package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Step of {@code POST /api/owners} that resolves the EFFECTIVE registration date and publishes it
 * for the later steps.
 *
 * <p>The effective date is the one supplied in the request body, or the server's current date when
 * the request omits one. Because a registration must fall on a business day, a date that lands on a
 * Saturday or Sunday is rolled forward to the next Monday — this applies whether the date was
 * supplied or defaulted.
 *
 * <p>Publishing the adjusted date once here keeps every value derived from it consistent: the daily
 * create-limit ({@link EnsureDailyLimit}) counts owners per this adjusted business day, the owner is
 * stored with it ({@link DefaultOwnerRegistrationDate}) and the membership number's year segment is
 * therefore taken from the adjusted date.
 */
public class ResolveRegistrationDate {

    public void service(@Val OwnerFieldsDto request, Out<LocalDate> registrationDate) {
        LocalDate effective = request.getRegistrationDate() != null
                ? request.getRegistrationDate() : LocalDate.now();
        while (effective.getDayOfWeek() == DayOfWeek.SATURDAY
                || effective.getDayOfWeek() == DayOfWeek.SUNDAY) {
            effective = effective.plusDays(1);
        }
        registrationDate.set(effective);
    }
}
