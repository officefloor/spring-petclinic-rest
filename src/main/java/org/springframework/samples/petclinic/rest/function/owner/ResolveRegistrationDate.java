package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Resolves the effective registration date for a create-owner request and publishes it for
 * later steps: the request's date when supplied, otherwise the server's current date, in
 * either case rolled forward to a business day (see {@link BusinessDay}). Runs before the
 * daily-limit guard so the cap counts per adjusted business day, and before the date is
 * applied to the owner so the membership number's year segment uses the adjusted value.
 */
public class ResolveRegistrationDate {

    public void service(@Val OwnerFieldsDto request, Out<LocalDate> registrationDate) {
        LocalDate supplied = request.getRegistrationDate();
        LocalDate effective = (supplied != null) ? supplied : LocalDate.now();
        registrationDate.set(BusinessDay.rollForward(effective));
    }
}
