package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Canonicalizes an owner's address form, whether supplied as the structured
 * {@code addressLine1}/{@code addressLine2} fields or the flat {@code address} field
 * (kept for backward compatibility). Each supplied field is normalized with
 * {@link AddressNormalizer}; the blank result of an absent field becomes {@code null}.
 *
 * <p>The structured form is preferred: when {@code addressLine1} is non-blank the flat
 * {@code address} is set to the composed value — the normalized first line, with a
 * single space and the normalized second line appended when a second line is present.
 * Otherwise the flat {@code address} is used as-is (normalized). Downstream steps and the
 * response therefore read one canonical {@code address} regardless of which form the
 * request used.
 */
final class OwnerAddress {

    private OwnerAddress() {
    }

    /** Normalize the request's address fields in place and set the composed {@code address}. */
    static void normalizeInto(OwnerFieldsDto request) {
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        String line2 = AddressNormalizer.normalize(request.getAddressLine2());
        String flat = AddressNormalizer.normalize(request.getAddress());
        request.setAddressLine1(emptyToNull(line1));
        request.setAddressLine2(emptyToNull(line2));
        request.setAddress(compose(line1, line2, flat));
    }

    /** The canonical address: structured first line (plus second line) when present, else the flat address. */
    private static String compose(String line1, String line2, String flat) {
        if (line1.isEmpty()) {
            return flat;
        }
        return line2.isEmpty() ? line1 : line1 + " " + line2;
    }

    private static String emptyToNull(String value) {
        return value.isEmpty() ? null : value;
    }
}
