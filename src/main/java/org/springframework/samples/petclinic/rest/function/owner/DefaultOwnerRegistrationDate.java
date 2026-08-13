package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Establishes a new owner's effective registration date: the date supplied in the request, or the
 * server's current date when the request omits it. The effective date must fall on a business day,
 * so a Saturday or Sunday — whether supplied or defaulted — rolls forward to the next Monday (see
 * {@link BusinessDay}). Runs after {@link BuildOwner} maps the request and before
 * {@link AssignOwnerMemberId} (which derives the {@code memberId}'s FY segment from this value) and
 * {@link SaveOwner} persists it.
 */
public class DefaultOwnerRegistrationDate {

    public void service(@Val Owner owner) {
        LocalDate effective = owner.getRegistrationDate() == null ? LocalDate.now()
                : owner.getRegistrationDate();
        owner.setRegistrationDate(BusinessDay.rollForward(effective));
    }
}
