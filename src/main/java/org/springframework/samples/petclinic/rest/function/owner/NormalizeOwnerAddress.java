package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidAddressException;

/**
 * Normalizes a new owner's address to its canonical stored form (see {@link AddressNormalizer}):
 * trims and collapses whitespace, upper-cases and expands common street-type abbreviations, so
 * {@code "  12  main  st "} is stored as {@code "12 MAIN STREET"}.
 *
 * <p>Structured input is preferred: when {@code addressLine1} is supplied (non-blank once
 * normalized) it, and the optional {@code addressLine2}, are normalized and stored, and the flat
 * {@code address} is composed from them — the normalized {@code addressLine1} with a single space
 * and the normalized {@code addressLine2} appended when present. Otherwise the flat {@code address}
 * input is normalized and stored, and no structured lines are kept. The stored (and later returned)
 * {@code address} is that normalized/composed string.
 *
 * <p>Enforces the required-field rule against the normalized form: an owner that supplies neither a
 * non-blank {@code addressLine1} nor a non-blank flat {@code address} is a 400. Mutates the built
 * {@link Owner} in place so {@link SaveOwner} persists the normalized value and later steps read it.
 */
public class NormalizeOwnerAddress {

    public void service(@Val Owner owner) throws InvalidAddressException {
        String line1 = AddressNormalizer.normalize(owner.getAddressLine1());
        if (line1 != null) {
            String line2 = AddressNormalizer.normalize(owner.getAddressLine2());
            owner.setAddressLine1(line1);
            owner.setAddressLine2(line2);
            owner.setAddress(AddressNormalizer.compose(line1, line2));
            return;
        }
        String normalized = AddressNormalizer.normalize(owner.getAddress());
        if (normalized == null) {
            throw new InvalidAddressException("Address must not be blank after normalization");
        }
        owner.setAddressLine1(null);
        owner.setAddressLine2(null);
        owner.setAddress(normalized);
    }
}
