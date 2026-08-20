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
}
