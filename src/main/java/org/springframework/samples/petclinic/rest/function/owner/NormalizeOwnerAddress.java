package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidAddressException;

/**
 * Normalizes the owner's postal address in place (trim and collapse whitespace,
 * upper-case, expand common abbreviations) and rejects an address that is blank once
 * normalized. Runs before the household steps so duplicate detection and the shared
 * household id compare the normalized form.
 */
public class NormalizeOwnerAddress {

    public void service(@Val Owner owner) throws InvalidAddressException {
        String normalized = OwnerAddresses.normalize(owner.getAddress());
        if (normalized.isBlank()) {
            throw new InvalidAddressException("Address must not be blank");
        }
        owner.setAddress(normalized);
    }
}
