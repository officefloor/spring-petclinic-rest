package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidAddressException;

/**
 * Normalizes a new owner's address to its canonical stored form (see {@link AddressNormalizer}):
 * trims and collapses whitespace, upper-cases and expands common street-type abbreviations, so
 * {@code "  12  main  st "} is stored as {@code "12 MAIN STREET"}. The stored (and later returned)
 * value is the normalized string. Enforces the required-field rule against that normalized form:
 * an address that is blank once normalized is a 400. Mutates the built {@link Owner} in place so
 * {@link SaveOwner} persists the normalized value and the later household steps compare it.
 */
public class NormalizeOwnerAddress {

    public void service(@Val Owner owner) throws InvalidAddressException {
        String normalized = AddressNormalizer.normalize(owner.getAddress());
        if (normalized == null) {
            throw new InvalidAddressException("Address must not be blank after normalization");
        }
        owner.setAddress(normalized);
    }
}
