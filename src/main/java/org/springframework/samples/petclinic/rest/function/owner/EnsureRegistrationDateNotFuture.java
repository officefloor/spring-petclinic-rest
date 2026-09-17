package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Create-owner step: rejects the request with a 400 when it supplies a registrationDate later
 * than the server's current date. A request that omits the date is fine — it later defaults to
 * the server date in {@link ResolveOwnerRegistrationDate}. Runs after {@link ValidateOwnerFields}
 * (so the request body is published) and before the date is resolved.
 */
public class EnsureRegistrationDateNotFuture {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate supplied = request.getRegistrationDate();
        if (supplied != null && supplied.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException();
        }
    }
}
