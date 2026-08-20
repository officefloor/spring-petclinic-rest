package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Converts raw telephone numbers into E.164 form ('+' followed by 8 to 15 digits). A leading '+'
 * and country code are kept when present, otherwise country code '+61' is assumed and a single
 * leading '0' is dropped from the national digits. Spaces, dashes and brackets are stripped.
 *
 * <p>Not a function class (never wired into a YAML pipeline) — just shared logic for
 * {@link NormalizeOwnerTelephone} and {@link CheckOwnerTelephoneUnique}.
 */
public final class TelephoneE164 {

    private TelephoneE164() {
    }

    /**
     * Converts {@code telephone} into E.164, or returns {@code null} when it cannot form a valid
     * E.164 number.
     */
    public static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        // Strip spaces, dashes and brackets.
        String cleaned = telephone.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            // Keep the explicit country code; the remainder must be all digits.
            digits = cleaned.substring(1);
        }
        else {
            // Assume '+61' and drop a single leading '0' from the national digits.
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Checks the national-number length of an E.164 number against its country code: '+61'
     * requires 9 national digits and '+1' requires 10. Country codes without a per-country length
     * rule here pass (only the generic 8-to-15-digit bound applies to them). Expects {@code e164}
     * already in valid E.164 form (a '+' followed by 8 to 15 digits), as returned by
     * {@link #toE164}; a {@code null} or non-E.164 value fails.
     */
    public static boolean nationalLengthValid(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return false;
        }
        String digits = e164.substring(1);
        // Longest known country code first: '+1' would also prefix-match, but '+61' never
        // starts with '1' and '+1' never starts with '61', so the two do not collide.
        if (digits.startsWith("61")) {
            return digits.length() - 2 == 9;
        }
        if (digits.startsWith("1")) {
            return digits.length() - 1 == 10;
        }
        return true;
    }
}
