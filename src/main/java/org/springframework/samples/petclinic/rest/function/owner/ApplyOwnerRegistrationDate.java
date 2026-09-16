package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Stamps the built owner with the business-day registration date resolved earlier by
 * {@link ResolveOwnerRegistrationDate}, replacing whatever raw date the request carried. Runs
 * after {@link BuildOwner} and before the owner is saved, so the persisted date — and every value
 * derived from it, such as the membership number's year segment — uses the adjusted date.
 */
public class ApplyOwnerRegistrationDate {

    public void service(@Val Owner owner, @Val LocalDate registrationDate) {
        owner.setRegistrationDate(registrationDate);
    }
}
