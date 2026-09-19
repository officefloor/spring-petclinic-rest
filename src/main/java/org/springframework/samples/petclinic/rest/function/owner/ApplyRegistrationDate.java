package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Applies the effective registration date (resolved and rolled to a business day by
 * {@link ResolveRegistrationDate}) to a newly built owner, replacing any raw date the request
 * body supplied. Runs before {@link AssignMemberId} so the member id's FY segment uses
 * the adjusted date.
 */
public class ApplyRegistrationDate {

    public void service(@Val Owner owner, @Val LocalDate registrationDate) {
        owner.setRegistrationDate(registrationDate);
    }
}
