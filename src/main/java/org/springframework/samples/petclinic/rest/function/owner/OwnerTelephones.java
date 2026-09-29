package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Single definition of how an owner telephone is converted to E.164 form: strip spaces,
 * dashes and brackets; keep a leading '+' and country code when present, otherwise assume
 * country code '+61' and drop a single leading '0' from the national digits; require 8 to
 * 15 digits after the '+'. Both {@link NormalizeOwnerTelephone} (which normalizes the
 * incoming request) and {@link EnsureUniqueOwnerTelephone} (which normalizes stored owners
 * for comparison) call this, so the create-time rule and the dedup comparison can never
 * silently diverge.
 */
final class OwnerTelephones {

    private OwnerTelephones() {
    }

    /**
     * Converts {@code telephone} to its E.164 representation (e.g. "0412 345 678" ->
     * "+61412345678"), or returns {@code null} when it cannot form a valid E.164 number.
     */
    static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        // Strip spaces, dashes and brackets; anything else must already be digits (or the
        // leading '+'), so a stray character makes the digit test below fail.
        String cleaned = telephone.replaceAll("[\\s()\\[\\]-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            // Explicit country code: keep it, just take the following digits.
            digits = cleaned.substring(1);
        } else {
            // No country code: assume '+61' and drop a single leading '0' from the national part.
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        return "+" + digits;
    }
}
