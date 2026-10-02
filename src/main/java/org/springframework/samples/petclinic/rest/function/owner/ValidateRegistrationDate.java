package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Rejects a supplied registrationDate that is later than the server's current date with a
 * 400, before any entity is built or persisted. A no-op when no registrationDate is
 * supplied (it then defaults to the server's current date in {@link BuildOwner}).
 *
 * <p>Reads the body published by {@link ValidateNewOwner} rather than binding
 * {@code @RequestBody} again, since the HTTP body may be bound only once per pipeline.
 */
public class ValidateRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate supplied = request.getRegistrationDate();
        if (supplied != null && supplied.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(
                    "registrationDate is later than the server date: " + supplied);
        }
    }
}
