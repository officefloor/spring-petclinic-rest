package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Resolves the EFFECTIVE registration date for the request and rolls it onto a business day, then
 * publishes it as a variable so every later step derives from the same adjusted date.
 *
 * <p>A supplied date later than the server's current date is rejected with a 400 (see
 * {@link FutureRegistrationDateException}) before any adjustment, since a registration cannot be
 * dated in the future.
 *
 * <p>The effective date is the one supplied in the request body, or the server's current date when
 * the request omits it. Either way, a weekend date is rolled forward to the next Monday (see
 * {@link BusinessDay}). {@link CheckDailyOwnerLimit} counts existing owners against this adjusted
 * day, {@link BuildOwner} stores it as the owner's {@code registrationDate}, and
 * {@link AssignMembershipNumber} derives the year segment from it.
 */
public class ResolveRegistrationDate {

    public void service(@Val OwnerFieldsDto request, Out<LocalDate> registrationDate)
            throws FutureRegistrationDateException {
        LocalDate serverDate = LocalDate.now();
        LocalDate supplied = request.getRegistrationDate();
        if (supplied != null && supplied.isAfter(serverDate)) {
            throw new FutureRegistrationDateException(supplied, serverDate);
        }
        LocalDate effective = supplied != null ? supplied : serverDate;
        registrationDate.set(BusinessDay.adjust(effective));
    }
}
