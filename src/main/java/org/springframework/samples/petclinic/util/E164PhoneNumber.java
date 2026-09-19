package org.springframework.samples.petclinic.util;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Converts a raw telephone number to <a href="https://en.wikipedia.org/wiki/E.164">E.164</a>
 * form: a leading '+' followed by 8 to 15 digits.
 *
 * <p>An explicit leading '+' and country code are kept; otherwise the default country code
 * {@code +61} is assumed and a single leading '0' is dropped from the national digits.
 * Spaces, dashes and brackets are stripped. So {@code "0412 345 678"} becomes
 * {@code "+61412345678"} and {@code "+64 21 123 456"} becomes {@code "+6421123456"}.
 */
public final class E164PhoneNumber {

    /** Country code assumed when the input does not carry an explicit '+' prefix. */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    /** Separators removed before parsing: spaces, dashes and brackets. */
    private static final Pattern SEPARATORS = Pattern.compile("[\\s()\\-]");

    /** A well-formed E.164 number: '+' then 8 to 15 digits. */
    private static final Pattern E164 = Pattern.compile("\\+[0-9]{8,15}");

    private E164PhoneNumber() {
    }

    /**
     * @param raw the caller-supplied telephone, in any common format
     * @return the E.164 string, or empty when {@code raw} cannot form a valid one
     */
    public static Optional<String> toE164(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String trimmed = raw.trim();
        boolean explicitCountryCode = trimmed.startsWith("+");
        String cleaned = SEPARATORS.matcher(trimmed).replaceAll("");
        String digits;
        if (explicitCountryCode) {
            digits = cleaned.substring(1); // drop the '+', keep the country code
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = DEFAULT_COUNTRY_CODE + national;
        }
        String candidate = "+" + digits;
        return E164.matcher(candidate).matches() ? Optional.of(candidate) : Optional.empty();
    }
}
