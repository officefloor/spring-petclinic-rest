package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Resolves the EFFECTIVE registration date for the request and rolls it onto a business day, then
 * publishes it as a variable so every later step derives from the same adjusted date.
 *
 * <p>The effective date is the one supplied in the request body, or the server's current date when
 * the request omits it. Either way, a weekend date is rolled forward to the next Monday (see
 * {@link BusinessDay}). {@link CheckDailyOwnerLimit} counts existing owners against this adjusted
 * day, {@link BuildOwner} stores it as the owner's {@code registrationDate}, and
 * {@link AssignMembershipNumber} derives the year segment from it.
 */
public class ResolveRegistrationDate {

    public void service(@Val OwnerFieldsDto request, Out<LocalDate> registrationDate) {
        LocalDate effective = request.getRegistrationDate() != null
                ? request.getRegistrationDate() : LocalDate.now();
        registrationDate.set(BusinessDay.adjust(effective));
    }
}
