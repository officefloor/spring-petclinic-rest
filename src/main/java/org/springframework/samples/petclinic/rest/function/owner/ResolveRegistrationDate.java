package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.function.common.BusinessDay;

/**
 * Step of {@code POST /api/owners} that resolves the effective registration date and adjusts it to a
 * business day. The effective date is the supplied {@code registrationDate} when present, otherwise
 * the server's current date; a Saturday, Sunday or public holiday is rolled forward to the next
 * non-holiday business day.
 *
 * <p>Runs before {@link RejectDailyOwnerLimit} and {@link BuildOwner} and publishes the adjusted
 * date as a {@code LocalDate} variable, so the daily create-limit counts owners per adjusted
 * business day, the new owner is stored with the adjusted date, and any value derived from it (the
 * membership number's year segment) uses the adjusted date too.
 */
public class ResolveRegistrationDate {

    public void service(@Val OwnerFieldsDto request, Out<LocalDate> registrationDate) {
        LocalDate effective = request.getRegistrationDate() != null
                ? request.getRegistrationDate() : LocalDate.now();
        registrationDate.set(BusinessDay.rollForward(effective));
    }
}
