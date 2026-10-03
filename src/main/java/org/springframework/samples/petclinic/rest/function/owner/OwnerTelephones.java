package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Single source of truth for what a "normalized" owner telephone is: the number in
 * <a href="https://en.wikipedia.org/wiki/E.164">E.164</a> form. Used by
 * {@link NormalizeOwnerTelephone} when storing and by {@link EnsureUniqueHousehold} when
 * forming the telephone part of an owner's identity key, so the rule cannot drift between
 * the two steps.
 *
 * <p>The rule: spaces, dashes and brackets are stripped. If a leading {@code '+'} (and
 * therefore an explicit country code) is present it is kept; otherwise country code
 * {@code '+61'} is assumed and a single leading {@code '0'} is dropped from the national
 * digits. The result must have 8 to 15 digits after the {@code '+'}, and for a recognized
 * country code the national number must have the exact length that country requires
 * ({@code '+61'} => 9 national digits, {@code '+1'} => 10), else it is invalid.
 */
final class OwnerTelephones {

    private OwnerTelephones() {
    }

    /**
     * Returns the E.164 form of {@code telephone}, or {@code null} if it cannot form a
     * valid E.164 number (non-digit characters remaining, not 8-15 digits after the
     * {@code '+'}, or a wrong national-number length for a recognized country code).
     */
    static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String trimmed = telephone.trim();
        boolean explicitCountryCode = trimmed.startsWith("+");
        // Strip spaces, dashes and brackets; keep a leading '+' and the digits.
        String stripped = trimmed.replaceAll("[\\s()\\-]", "");
        String digits = explicitCountryCode ? stripped.substring(1) : stripped;
        if (!digits.matches("\\d+")) {
            return null;
        }
        if (!explicitCountryCode) {
            // No country code: assume +61 and drop a single leading national '0'.
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            digits = "61" + digits;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            return null;
        }
        if (!hasValidNationalLength(digits)) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Checks the national-number length against the country code: {@code '+61'} requires
     * exactly 9 national digits and {@code '+1'} exactly 10. Unrecognized country codes are
     * accepted on the general 8-15 digit rule alone.
     */
    private static boolean hasValidNationalLength(String digits) {
        if (digits.startsWith("61")) {
            return digits.length() - 2 == 9;
        }
        if (digits.startsWith("1")) {
            return digits.length() - 1 == 10;
        }
        return true;
    }
}
