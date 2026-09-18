package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerFieldsException;

/**
 * Runs after {@link ValidateOwnerPostcode} in {@code POST /api/owners}, reading the
 * already-validated body as a variable. When a {@code registrationDate} is supplied it may
 * not be later than the server's current date; a future date throws
 * {@link InvalidOwnerFieldsException} naming {@code registrationDate} for a 400. An absent
 * date is left untouched (it defaults to the server date downstream). Runs before
 * {@link ResolveOwnerRegistrationDate}, which then rolls the accepted date off weekends.
 */
public class ValidateOwnerRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws InvalidOwnerFieldsException {
        LocalDate supplied = request.getRegistrationDate();
        if (supplied != null && supplied.isAfter(LocalDate.now())) {
            throw new InvalidOwnerFieldsException(List.of("registrationDate"));
        }
    }
}
