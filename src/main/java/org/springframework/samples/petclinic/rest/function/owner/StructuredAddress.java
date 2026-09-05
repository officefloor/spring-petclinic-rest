package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Applies the structured address form to a request in place. When a non-blank
 * {@code addressLine1} is supplied it is preferred: {@code addressLine1} and
 * {@code addressLine2} are normalised ({@link NormaliseAddress}) and the flat
 * {@code address} is composed as the normalised {@code addressLine1} with the normalised
 * {@code addressLine2} appended after a single space when present. Otherwise the flat
 * {@code address} is normalised on its own, so earlier minimal (flat-address) requests
 * stay accepted. Either form yields a non-blank {@code address} exactly when the request
 * supplied an address, which the required-field check then enforces.
 */
public final class StructuredAddress {

    private StructuredAddress() {
    }

    public static void apply(OwnerFieldsDto request) {
        String line1 = NormaliseAddress.normalise(request.getAddressLine1());
        if (line1.isEmpty()) {
            request.setAddress(NormaliseAddress.normalise(request.getAddress()));
            return;
        }
        String line2 = NormaliseAddress.normalise(request.getAddressLine2());
        request.setAddressLine1(line1);
        request.setAddressLine2(line2.isEmpty() ? null : line2);
        request.setAddress(line2.isEmpty() ? line1 : line1 + " " + line2);
    }
}
