package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Rejects a create-owner request whose supplied {@code registrationDate} is later than the server's
 * current date, by throwing {@link FutureRegistrationDateException} (handled as 400). Registration
 * must not be dated in the future. The field is optional: when absent it is not validated (it defaults
 * to the server date on {@link BuildOwner build}).
 */
public class ValidateOwnerRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate requested = request.getRegistrationDate();
        if (requested != null) {
            LocalDate serverDate = LocalDate.now();
            if (requested.isAfter(serverDate)) {
                throw new FutureRegistrationDateException(requested, serverDate);
            }
        }
    }
}
