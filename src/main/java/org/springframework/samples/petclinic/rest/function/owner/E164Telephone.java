package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Converts a raw telephone into canonical E.164 form: a leading '+', a country code and
 * the national number with no separators. A number that already carries an explicit
 * '+&lt;country code&gt;' keeps it; otherwise the Australian country code '+61' is assumed
 * and a single leading '0' is dropped from the national digits. Spaces, dashes and
 * brackets are stripped, and the result must hold 8 to 15 digits after the '+', else the
 * number cannot form valid E.164 and is rejected with an {@link InvalidTelephoneException}.
 *
 * <p>For a recognised country code the national number must also be exactly the length that
 * country requires ('+61' expects 9 national digits, '+1' expects 10); a wrong length is
 * rejected with an {@link InvalidTelephoneException}. Unrecognised country codes are held to
 * the generic 8-to-15-digit rule only.
 *
 * <p>The single, shared definition of a normalized telephone: {@link NormalizeOwnerTelephone}
 * uses it to store the E.164 value, which then forms the telephone part of the owner's
 * {@link IdentityKey} used for duplicate detection.
 */
final class E164Telephone {

    /** Country code assumed when the number carries no explicit '+' prefix. */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    /** Recognised country code -> exact number of national digits it requires. */
    private static final Map<String, Integer> NATIONAL_DIGITS = Map.of("61", 9, "1", 10);

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
        checkNationalLength(digits);
        return "+" + digits;
    }

    /**
     * Enforce the national-number length required by a recognised country code. A number
     * whose leading digits match a known code but whose remaining (national) digits are the
     * wrong length is rejected; codes not in {@link #NATIONAL_DIGITS} are left to the caller's
     * generic length check.
     */
    private static void checkNationalLength(String digits) throws InvalidTelephoneException {
        for (Map.Entry<String, Integer> country : NATIONAL_DIGITS.entrySet()) {
            String code = country.getKey();
            if (digits.startsWith(code)) {
                int nationalLength = digits.length() - code.length();
                if (nationalLength != country.getValue()) {
                    throw new InvalidTelephoneException();
                }
                return;
            }
        }
    }
}
