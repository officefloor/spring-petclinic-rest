package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Rolls the effective registration date onto a business day: a Saturday, Sunday or
 * listed public holiday &mdash; whether supplied in the request or defaulted by
 * {@link DefaultOwnerRegistrationDate} &mdash; moves forward to the next non-holiday
 * business day.
 * Mutates the shared request in place so the adjusted date is the one the daily limit
 * counts against, the membership number's year segment is taken from, and
 * {@link BuildOwner} persists. Runs after {@link DefaultOwnerRegistrationDate}, so the
 * date is always present.
 */
public class RollRegistrationDateToBusinessDay {

    public void service(@Val OwnerFieldsDto request) {
        request.setRegistrationDate(BusinessDays.rollForward(request.getRegistrationDate()));
    }
}
