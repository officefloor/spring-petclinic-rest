package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Rejects a create-owner request whose supplied {@code registrationDate} is later than the
 * server date. A registration date may be back-dated or omitted (defaulted to today by
 * {@link BusinessDay}), but a future date is not allowed. Reads the request DTO, so it runs
 * before {@link BuildOwner} and against the raw supplied date (before any weekend roll).
 * On a future date raises {@link FutureRegistrationDateException} (400).
 */
public class CheckRegistrationDateNotFuture {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate supplied = request.getRegistrationDate();
        if (supplied != null) {
            LocalDate today = LocalDate.now();
            if (supplied.isAfter(today)) {
                throw new FutureRegistrationDateException(supplied, today);
            }
        }
    }
}
