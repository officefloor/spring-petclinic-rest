package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Validates the owner's {@code registrationDate} WHEN SUPPLIED, so a request that omits one stays
 * accepted (the server defaults it in {@link ResolveRegistrationDate}). A supplied date must not be
 * later than the server's current date; a future date is rejected with 400 via
 * {@link FutureRegistrationDateException}.
 *
 * <p>Reads the body published by the preceding validate step via {@code @Val} and runs before the
 * date is resolved/defaulted, so a future date is a 400 rather than being masked by a later step.
 */
public class ValidateRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate supplied = request.getRegistrationDate();
        if (supplied == null) {
            return; // optional when absent; the server defaults it later
        }
        LocalDate serverDate = LocalDate.now();
        if (supplied.isAfter(serverDate)) {
            throw new FutureRegistrationDateException(supplied, serverDate);
        }
    }
}
