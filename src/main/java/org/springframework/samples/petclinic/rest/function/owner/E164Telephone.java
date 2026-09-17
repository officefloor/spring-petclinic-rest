package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Converts a raw telephone into canonical E.164 form: a leading '+', a country code and
 * the national number with no separators. A number that already carries an explicit
 * '+&lt;country code&gt;' keeps it; otherwise the Australian country code '+61' is assumed
 * and a single leading '0' is dropped from the national digits. Spaces, dashes and
 * brackets are stripped, and the result must hold 8 to 15 digits after the '+', else the
 * number cannot form valid E.164 and is rejected with an {@link InvalidTelephoneException}.
 *
 * <p>The single, shared definition of a normalized telephone: {@link NormalizeOwnerTelephone}
 * uses it to store the E.164 value and {@link EnsureUniqueTelephone} uses it to compare
 * telephones for duplicates.
 */
final class E164Telephone {

    /** Country code assumed when the number carries no explicit '+' prefix. */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    private static final int MIN_DIGITS = 8;

    private static final int MAX_DIGITS = 15;

    private E164Telephone() {
    }

    /**
     * Normalize {@code telephone} to E.164, or throw when it cannot form a valid number.
     */
    static String normalize(String telephone) throws InvalidTelephoneException {
        if (telephone == null) {
            throw new InvalidTelephoneException();
        }
        String trimmed = telephone.trim();
        boolean explicitCountryCode = trimmed.startsWith("+");
        String cleaned = trimmed.replaceAll("[\\s()\\-]", "");
        if (explicitCountryCode) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.isEmpty() || !cleaned.chars().allMatch(c -> c >= '0' && c <= '9')) {
            throw new InvalidTelephoneException();
        }
        String digits;
        if (explicitCountryCode) {
            digits = cleaned;
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = DEFAULT_COUNTRY_CODE + national;
        }
        if (digits.length() < MIN_DIGITS || digits.length() > MAX_DIGITS) {
            throw new InvalidTelephoneException();
        }
        return "+" + digits;
    }
}
