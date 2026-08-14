package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs in the create-owner pipeline after {@link BuildOwner}. Resolves the effective registration
 * date — the value supplied in the request, or the server's current date when the request omitted
 * one — and adjusts it to a business day via {@link BusinessDays}: a Saturday or Sunday rolls
 * forward to the next Monday. The adjusted date is stored on the built owner in place, so later
 * steps derive from and respond with the resolved business-day value (formatted as ISO 'YYYY-MM-DD').
 */
public class DefaultOwnerRegistrationDate {

    public void service(@Val Owner owner) {
        LocalDate effective = owner.getRegistrationDate() != null
                ? owner.getRegistrationDate()
                : LocalDate.now();
        owner.setRegistrationDate(BusinessDays.adjust(effective));
    }
}
