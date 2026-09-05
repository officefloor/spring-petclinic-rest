package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Reconciles an owner's address input, which may arrive in either of two backward-compatible
 * forms:
 *
 * <ul>
 * <li>the structured fields {@code addressLine1} (required, non-blank) plus an optional
 * {@code addressLine2}; or</li>
 * <li>the legacy flat {@code address} single-line field.</li>
 * </ul>
 *
 * <p>The structured fields are preferred when present. {@link #normalizeInPlace(OwnerFieldsDto)}
 * normalizes whichever address fields are supplied (via {@link AddressNormalizer}) and sets the
 * flat {@code address} to the composed line — the normalized {@code addressLine1}, with a single
 * space and the normalized {@code addressLine2} appended when {@code addressLine2} is present —
 * so every later step and the response see a single canonical {@code address} while the
 * structured lines are stored and returned as supplied.
 *
 * <p>A plain utility (not an OfficeFloor function), so it may expose helpers without tripping the
 * one-public-method-per-function rule.
 */
public final class OwnerAddress {

    private OwnerAddress() {
    }

    /**
     * Normalizes the supplied address fields in place and (re)composes the flat {@code address}.
     * Absent fields are left untouched. Prefers the structured fields when {@code addressLine1}
     * is present, otherwise keeps the normalized flat {@code address}.
     */
    public static void normalizeInPlace(OwnerFieldsDto request) {
        boolean hasLine1 = isPresent(request.getAddressLine1());
        boolean hasLine2 = isPresent(request.getAddressLine2());
        if (hasLine1) {
            request.setAddressLine1(AddressNormalizer.normalize(request.getAddressLine1()));
        }
        if (hasLine2) {
            request.setAddressLine2(AddressNormalizer.normalize(request.getAddressLine2()));
        }
        if (isPresent(request.getAddress())) {
            request.setAddress(AddressNormalizer.normalize(request.getAddress()));
        }
        if (hasLine1) {
            String composed = request.getAddressLine1();
            if (hasLine2) {
                composed = composed + " " + request.getAddressLine2();
            }
            request.setAddress(composed);
        }
    }

    /**
     * @return {@code true} when the request supplies an address in either form — a non-blank
     *         {@code addressLine1} or a non-blank flat {@code address}.
     */
    public static boolean hasAddress(OwnerFieldsDto request) {
        return isPresent(request.getAddressLine1()) || isPresent(request.getAddress());
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
