package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerRegistrationDateInvalidException;

/**
 * Validates the owner's optional {@code registrationDate} WHEN PRESENT: a supplied value must not be
 * later than the server's current date, since a registration cannot be dated in the future. An
 * absent date is left untouched (it later defaults to the server date in {@link BuildOwner}).
 *
 * <p>Reads the body published by {@link RequireOwnerFields}; runs before {@link BuildOwner} so a
 * future date is a 400 via {@link OwnerRegistrationDateInvalidException}, never stored.
 */
public class RequireRegistrationDateNotFuture {

    public void service(@Val OwnerFieldsDto request) throws OwnerRegistrationDateInvalidException {
        LocalDate supplied = request.getRegistrationDate();
        if (supplied != null && supplied.isAfter(LocalDate.now())) {
            throw new OwnerRegistrationDateInvalidException(supplied);
        }
    }
}
