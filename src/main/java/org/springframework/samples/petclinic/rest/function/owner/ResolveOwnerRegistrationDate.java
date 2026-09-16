package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Resolves the effective registration date for a new owner — the date supplied in the request, or
 * the server's current date when the request omitted one — and rolls a weekend onto the next
 * business day via {@link BusinessDays}. Runs before {@link RejectDailyOwnerLimit} so the daily
 * cap counts against this same business day, and publishes the date once so
 * {@link ApplyOwnerRegistrationDate} stamps the owner with exactly the value that was counted.
 */
public class ResolveOwnerRegistrationDate {

    public void service(@Val OwnerFieldsDto request, Out<LocalDate> registrationDate) {
        LocalDate effective = request.getRegistrationDate() != null
                ? request.getRegistrationDate() : LocalDate.now();
        registrationDate.set(BusinessDays.rollForward(effective));
    }
}
