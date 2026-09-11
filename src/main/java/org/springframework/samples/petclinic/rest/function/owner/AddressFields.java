package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes the address fields on an owner request in place, preferring the structured
 * {@code addressLine1}/{@code addressLine2} pair over the flat {@code address} when it is
 * present (see {@link AddressNormalizer}), and stores the composed canonical address back
 * on {@code address} so everything downstream reads a single normalized value.
 *
 * <p>The composed {@code address} is the normalized {@code addressLine1}, with a single
 * space and the normalized {@code addressLine2} appended when {@code addressLine2} is
 * present. When only the flat {@code address} is supplied it is normalized and the
 * structured lines are cleared, keeping earlier flat-address requests backward-compatible.
 */
final class AddressFields {

    private AddressFields() {
    }

    /**
     * Normalizes the request's address fields in place. Returns {@code true} when the
     * request supplies an address in either form (a non-blank {@code addressLine1} or a
     * non-blank flat {@code address}); {@code false} when neither is present, in which
     * case nothing is stored back.
     */
    static boolean normalizeInto(OwnerFieldsDto request) {
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        if (!line1.isEmpty()) {
            String line2 = AddressNormalizer.normalize(request.getAddressLine2());
            request.setAddressLine1(line1);
            if (line2.isEmpty()) {
                request.setAddressLine2(null);
                request.setAddress(line1);
            }
            else {
                request.setAddressLine2(line2);
                request.setAddress(line1 + " " + line2);
            }
            return true;
        }
        String flat = AddressNormalizer.normalize(request.getAddress());
        if (!flat.isEmpty()) {
            request.setAddress(flat);
            request.setAddressLine1(null);
            request.setAddressLine2(null);
            return true;
        }
        return false;
    }
}
