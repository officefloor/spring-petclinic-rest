package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Step of {@code POST /api/owners} that rejects a supplied registration date in the future. Runs
 * only WHEN a registrationDate is present: an owner created without one defaults to the server date
 * later (see {@link DetermineRegistrationDate}), so absence is accepted unchanged.
 *
 * <p>When present, the raw supplied date must not be later than the server's current date. A future
 * date is rejected with {@link FutureRegistrationDateException} (400). This runs before
 * {@link DetermineRegistrationDate}, so the check applies to the value the client sent, not the
 * business-day-adjusted date.
 */
public class RequireNotFutureRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate date = request.getRegistrationDate();
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(date);
        }
    }
}
