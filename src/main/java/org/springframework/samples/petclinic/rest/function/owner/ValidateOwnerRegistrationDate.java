package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-time guard for {@code POST /owners}: validates {@code registrationDate} <em>only when
 * present</em>. A supplied registration date may not be later than the server's current date;
 * otherwise the request is rejected with 400 via {@link FutureRegistrationDateException}. An absent
 * (null) registration date is accepted unchanged — it defaults to the server date later in the
 * pipeline ({@link BuildOwner}) — so the request contract stays backward-compatible.
 */
public class ValidateOwnerRegistrationDate {

    public void service(@Val OwnerFieldsDto request) throws FutureRegistrationDateException {
        LocalDate registrationDate = request.getRegistrationDate();
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(registrationDate);
        }
    }
}
