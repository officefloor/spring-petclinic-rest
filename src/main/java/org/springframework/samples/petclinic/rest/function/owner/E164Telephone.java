package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

/**
 * Converts a raw telephone into E.164 form. A leading {@code '+'} and country code are kept
 * when present; otherwise country code {@code '+61'} is assumed and a single leading {@code '0'}
 * is dropped from the national digits. Spaces, dashes and brackets are stripped, and the result
 * must carry 8 to 15 digits after the {@code '+'}.
 *
 * <p>So {@code "0412 345 678"} becomes {@code "+61412345678"} and {@code "+64 21 123 456"}
 * becomes {@code "+6421123456"}.
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
        return E164_DIGITS.matcher(digits).matches() ? "+" + digits : null;
    }
}
