package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

/**
 * Normalizes a telephone number to E.164 form.
 *
 * <p>Rules: strip spaces, dashes and brackets; keep a leading '+' and country code when
 * present, otherwise assume country code '+61' and drop a single leading '0' from the
 * national digits. The result is {@code '+'} followed by 8 to 15 digits; anything that
 * cannot form a valid E.164 number yields {@code null}.
 *
 * <p>Example: {@code "0412 345 678"} becomes {@code "+61412345678"}.
 */
final class TelephoneE164 {

    /** Characters removed before interpreting the number: spaces, dashes and brackets. */
    private static final Pattern SEPARATORS = Pattern.compile("[\\s\\-()]");

    private TelephoneE164() {
    }

    /**
     * Convert to E.164, or return {@code null} when the input cannot form a valid number.
     */
    static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        boolean hasCountryCode = trimmed.startsWith("+");
        String cleaned = SEPARATORS.matcher(trimmed).replaceAll("");
        String digits;
        if (hasCountryCode) {
            // Keep the given country code; the '+' is re-added below.
            digits = cleaned.substring(1);
        }
        else {
            // No country code: assume '+61' and drop a single leading national '0'.
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.chars().allMatch(Character::isDigit) || digits.isEmpty()) {
            return null;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            return null;
        }
        return "+" + digits;
    }
}
