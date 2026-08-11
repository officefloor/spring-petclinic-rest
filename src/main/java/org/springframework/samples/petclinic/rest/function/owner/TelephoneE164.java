package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Converts a free-form telephone into E.164 form.
 *
 * <p>Rules: when a leading {@code '+'} is present the following digits are taken as the
 * country code plus national number as given; otherwise country code {@code '+61'} is
 * assumed and a single leading {@code '0'} is dropped from the national digits. Spaces,
 * dashes, brackets and any other non-digit characters are stripped. A valid result has
 * between 8 and 15 digits after the {@code '+'}.
 */
final class TelephoneE164 {

    private TelephoneE164() {
    }

    /**
     * Converts {@code raw} to its E.164 candidate string (a leading {@code '+'} followed
     * by digits), or {@code null} when it carries no digits at all. Length is not enforced
     * here — call {@link #isValid(String)} to check the 8..15 digit rule.
     */
    static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        boolean hasPlus = trimmed.startsWith("+");
        String digits = trimmed.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return null;
        }
        if (hasPlus) {
            return "+" + digits;
        }
        String national = digits.startsWith("0") ? digits.substring(1) : digits;
        return "+61" + national;
    }

    /** True when {@code e164} is a {@code '+'} followed by 8 to 15 digits. */
    static boolean isValid(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return false;
        }
        int digitCount = e164.length() - 1;
        return digitCount >= 8 && digitCount <= 15;
    }
}
