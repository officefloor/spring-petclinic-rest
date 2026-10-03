package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidAddressException;

/**
 * Normalizes the owner's postal address in place (trim and collapse whitespace,
 * upper-case, expand common abbreviations) and rejects an owner that supplies no
 * address in either form. Runs before the household steps so duplicate detection and
 * the shared household id compare the normalized form.
 *
 * <p>The structured fields are preferred over the flat {@code address} and the canonical
 * normalized forms come from {@link OwnerAddresses#resolve}; this step stores them and
 * rejects with 400 when the owner supplied no address in either form.
 */
public class NormalizeOwnerAddress {

    public void service(@Val Owner owner) throws InvalidAddressException {
        OwnerAddresses.Normalized normalized = OwnerAddresses.resolve(
                owner.getAddressLine1(), owner.getAddressLine2(), owner.getAddress());
        if (normalized.address().isBlank()) {
            throw new InvalidAddressException("Address must not be blank");
        }
        owner.setAddressLine1(normalized.addressLine1());
        owner.setAddressLine2(normalized.addressLine2());
        owner.setAddress(normalized.address());
    }
}
