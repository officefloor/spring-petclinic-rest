package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

/**
 * Step of {@code POST /api/owners}: validates the optional {@code registrationDate} WHEN PRESENT. A
 * supplied date may not be later than the server's current date, so a future date is rejected 400
 * via {@link FutureRegistrationDateException}. An absent registrationDate is left untouched (the
 * server later defaults it to the current date), keeping the request contract backward-compatible.
 *
 * <p>Runs before {@link ApplyRegistrationDate} and {@link RequireDailyCapacity} so the raw supplied
 * date is checked before any business-day adjustment is applied to it.
 */
public class RequireCurrentRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate supplied = request.getRegistrationDate();
        if (supplied != null && supplied.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(supplied);
        }
    }
}
