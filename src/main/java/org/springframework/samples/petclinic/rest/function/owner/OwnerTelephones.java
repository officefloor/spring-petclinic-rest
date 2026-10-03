package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Single source of truth for the owner "E.164 telephone" rule. Both
 * {@link NormalizeOwnerTelephone} (which stores the E.164 value) and
 * {@link RejectDuplicateTelephone} (which compares against it) go through here, so the two
 * can never drift apart.
 *
 * <p>The rule: strip spaces, dashes and brackets; keep a leading {@code '+'} and country
 * code when present, otherwise assume country code {@code '+61'} and drop a single leading
 * {@code '0'} from the national digits; require 8 to 15 digits after the {@code '+'}. So
 * {@code "0412 345 678"} becomes {@code "+61412345678"}.
 */
final class OwnerTelephones {

    private OwnerTelephones() {
    }

    /**
     * Converts {@code telephone} to its E.164 form, or returns {@code null} when it cannot
     * form a valid E.164 number (null input, non-digit content, or fewer than 8 / more than
     * 15 digits after the {@code '+'}).
     */
    static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        // Strip spaces, dashes and brackets; any other non-digit is left in place so it
        // fails the digits check below.
        String cleaned = telephone.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            // Explicit country code: keep it, drop the leading '+'.
            digits = cleaned.substring(1);
        } else {
            // No country code: assume '+61' and drop a single leading '0' from the national
            // digits.
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        return "+" + digits;
    }
}
