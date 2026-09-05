package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Rejects a create-owner request whose supplied {@code registrationDate} is later than the current
 * server date. The date is optional: when absent the request is accepted unchanged (and
 * {@link BuildOwner} defaults it to the server date). A future date is rejected via
 * {@link FutureRegistrationDateException}, which the global handler turns into a 400. Runs before the
 * owner is built and saved.
 */
public class RejectFutureRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate supplied = request.getRegistrationDate();
        if (supplied == null) {
            return; // registrationDate is optional; nothing to validate when absent
        }
        LocalDate today = LocalDate.now();
        if (supplied.isAfter(today)) {
            throw new FutureRegistrationDateException(supplied, today);
        }
    }
}
