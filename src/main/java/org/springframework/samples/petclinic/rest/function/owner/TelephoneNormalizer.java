package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Single home for owner telephone normalization. Both the create pipeline's
 * {@link BuildOwner} (which stores the canonical form) and
 * {@link CheckOwnerTelephoneUnique} (which detects duplicates) go through here, so the
 * stored form and the duplicate-comparison stay defined in one place.
 */
final class TelephoneNormalizer {

    private TelephoneNormalizer() {
    }

    /** Default country code assumed when the supplied number carries no explicit '+'. */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    /**
     * Canonical E.164 form to store for a supplied telephone. Spaces, dashes and brackets
     * are stripped. A leading '+' with its country code is kept; otherwise country code
     * '+61' is assumed and a single leading '0' is dropped from the national digits. The
     * result must have 8 to 15 digits after the '+', else the request is rejected with a
     * 400. So '0412 345 678' becomes '+61412345678'.
     */
    static String normalize(String telephone) throws InvalidTelephoneException {
        String e164 = toE164(telephone);
        if (e164 == null) {
            throw new InvalidTelephoneException(telephone);
        }
        return e164;
    }

    /**
     * Comparison key used to detect duplicate telephones — the E.164 form. Lenient (never
     * throws) so whatever is already stored on existing owners still compares; when a value
     * cannot form valid E.164 it falls back to its stripped characters.
     */
    static String comparisonKey(String telephone) {
        String e164 = toE164(telephone);
        return e164 != null ? e164 : stripSeparators(telephone);
    }

    /**
     * Produce the E.164 string for a telephone, or {@code null} when it cannot form a valid
     * one (non-digit content remaining, or not 8 to 15 digits after the '+').
     */
    private static String toE164(String telephone) {
        String cleaned = stripSeparators(telephone);
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = DEFAULT_COUNTRY_CODE + national;
        }
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        return "+" + digits;
    }

    private static String stripSeparators(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("[\\s()\\-]", "");
    }
}
