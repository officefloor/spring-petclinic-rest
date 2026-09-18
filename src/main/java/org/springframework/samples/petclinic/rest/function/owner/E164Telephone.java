package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import org.springframework.samples.petclinic.model.E164;

/**
 * Converts a raw telephone into E.164 form. A leading {@code '+'} and country code are kept
 * when present; otherwise country code {@code '+61'} is assumed and a single leading {@code '0'}
 * is dropped from the national digits. Spaces, dashes and brackets are stripped, and the result
 * must carry 8 to 15 digits after the {@code '+'}.
 *
 * <p>So {@code "0412 345 678"} becomes {@code "+61412345678"} and {@code "+64 21 123 456"}
 * becomes {@code "+6421123456"}.
 *
 * <p>Where the country code has a known fixed national-number length, that length is enforced:
 * {@code +61} requires 9 national digits and {@code +1} requires 10. A number whose national
 * part is the wrong length for its country code is rejected. Country codes with no known rule
 * are constrained only by the general 8-to-15-digit bound.
 */
final class E164Telephone {

    /** Country code assumed when the number carries no explicit {@code '+'} prefix. */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    /** Spaces, dashes and brackets, which carry no meaning in E.164. */
    private static final Pattern SEPARATORS = Pattern.compile("[\\s()\\-]");

    /** A valid E.164 body: 8 to 15 digits following the {@code '+'}. */
    private static final Pattern E164_DIGITS = Pattern.compile("\\d{8,15}");

    private E164Telephone() {
    }

    /**
     * @param raw the telephone as supplied by the client.
     * @return the E.164 string (e.g. {@code "+61412345678"}), or {@code null} if {@code raw}
     *         cannot form a valid E.164 number.
     */
    static String format(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = SEPARATORS.matcher(raw).replaceAll("");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = DEFAULT_COUNTRY_CODE + national;
        }
        if (!E164_DIGITS.matcher(digits).matches() || !hasValidNationalLength(digits)) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Checks the national-number length against the number's country code, taken as the longest
     * known country-code prefix of {@code digits}.
     *
     * @param digits the E.164 body (country code plus national number, no {@code '+'}).
     * @return {@code false} only when {@code digits} starts with a country code that has a known
     *         national-number length and the national part does not match it; {@code true} for
     *         country codes without a known rule.
     */
    private static boolean hasValidNationalLength(String digits) {
        String countryCode = E164.countryCodeOf(digits);
        if (countryCode == null) {
            return true;
        }
        return digits.length() - countryCode.length() == E164.nationalLength(countryCode);
    }
}
