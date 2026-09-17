package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.BusinessDay;

/**
 * Resolves an owner's effective registration date and rolls it onto a business day. The effective
 * date is the one supplied on the create request, or the server's current date when the request
 * omits it. Either way, a Saturday, Sunday or public holiday rolls forward to the next non-holiday
 * business day (see {@link BusinessDay#onOrAfter(LocalDate)}). Runs before the customer code,
 * membership number and daily create-limit are derived, so all of them key off the adjusted date.
 * Mutates the built {@link Owner} in place.
 */
public class ResolveRegistrationDate {

    public void service(@Val Owner owner) {
        LocalDate effective = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        owner.setRegistrationDate(BusinessDay.onOrAfter(effective));
    }
}
