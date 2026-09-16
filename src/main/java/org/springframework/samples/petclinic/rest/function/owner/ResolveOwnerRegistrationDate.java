package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Establishes a new owner's effective registration date: defaulting to the server's current
 * date when the request did not supply one, then rolling a weekend date (whether supplied or
 * defaulted) forward to the next business day so the stored {@code registrationDate} always
 * falls on a weekday. Everything derived from it (e.g. the membership number's year segment,
 * the per-day create limit) therefore uses the adjusted date.
 */
public class ResolveOwnerRegistrationDate {

    public void service(@Val Owner owner) {
        LocalDate effective = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        owner.setRegistrationDate(BusinessDays.rollForward(effective));
    }
}
