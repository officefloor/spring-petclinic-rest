package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.BusinessDay;

/**
 * Resolves the effective registration date for {@code POST /api/owners}: the date supplied in
 * the body, or the server's current date when none was supplied, rolled forward to the next
 * business day when it falls on a weekend. Publishes it so the daily-capacity check counts
 * against the adjusted business day and {@link ApplyOwnerRegistrationDate} stamps the same
 * value onto the built owner. Runs after validation and before the daily-capacity check.
 */
public class ResolveOwnerRegistrationDate {

    public void service(@Val OwnerFieldsDto request, Out<LocalDate> registrationDate) {
        LocalDate supplied = request.getRegistrationDate();
        LocalDate effective = supplied != null ? supplied : LocalDate.now();
        registrationDate.set(BusinessDay.rollForward(effective));
    }
}
