package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Resolves an owner's address from either the structured fields (a non-blank addressLine1 plus an
 * optional addressLine2) or the flat {@code address}, preferring the structured form. Normalizes
 * whichever fields are supplied (see {@link AddressNormalizer}) and sets {@code address} to the
 * composed value: the normalized addressLine1, with a single space and the normalized addressLine2
 * appended when addressLine2 is present. Everything that later reads {@code address} therefore sees
 * the structured value when present, falling back to the flat address otherwise.
 */
public final class StructuredAddress {

    private StructuredAddress() {
    }

    public static void apply(Owner owner) {
        String line1 = AddressNormalizer.normalize(owner.getAddressLine1());
        if (line1.isEmpty()) {
            owner.setAddressLine1(null);
            owner.setAddressLine2(null);
            owner.setAddress(AddressNormalizer.normalize(owner.getAddress()));
            return;
        }
        String line2 = AddressNormalizer.normalize(owner.getAddressLine2());
        owner.setAddressLine1(line1);
        owner.setAddressLine2(line2.isEmpty() ? null : line2);
        owner.setAddress(line2.isEmpty() ? line1 : line1 + " " + line2);
    }
}
