package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Stamps the resolved registration date onto the built owner in {@code POST /api/owners}, so
 * it is persisted and returned as ISO 'YYYY-MM-DD' and every value derived from it (such as
 * the fiscal year and the membership number's year segment) uses the adjusted business day. The date is resolved
 * by {@link ResolveOwnerRegistrationDate} (supplied-or-server date, rolled off weekends).
 * Runs after {@link BuildOwner} and before the owner is saved.
 */
public class ApplyOwnerRegistrationDate {

    public void service(@Val Owner owner, @Val LocalDate registrationDate) {
        owner.setRegistrationDate(registrationDate);
    }
}
