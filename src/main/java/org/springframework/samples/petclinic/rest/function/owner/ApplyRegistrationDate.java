package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Stamps a newly built owner with the effective registration date resolved by
 * {@link ResolveRegistrationDate}. Runs after {@link BuildOwner} and mutates the built
 * owner in place, overwriting whatever date the request body mapped in, so the owner is
 * stored and returned with the adjusted business day and later steps (e.g.
 * {@link AssignMembershipNumber}) derive their values from it.
 */
public class ApplyRegistrationDate {

    public void service(@Val Owner owner, @Val LocalDate registrationDate) {
        owner.setRegistrationDate(registrationDate);
    }
}
