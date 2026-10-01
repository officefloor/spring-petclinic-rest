package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Single source of truth for the owner-telephone E.164 rule. Both {@link ValidateOwnerFields}
 * (which normalizes the submitted value) and {@link CheckOwnerTelephoneUnique} (which compares
 * against stored owners) convert through {@link #toE164} so the two can never drift apart.
 *
 * <p>The rule: strip spaces, dashes and brackets. If what remains starts with a '+', keep that
 * leading '+' and country code as-is. Otherwise assume Australian country code '+61', dropping a
 * single leading '0' from the national digits before prefixing. The result must be a '+' followed
 * by 8 to 15 digits; anything else cannot form valid E.164 and yields {@code null}.
 *
 * <p>Example: {@code "0412 345 678"} becomes {@code "+61412345678"}.
 */
final class OwnerTelephone {

    private OwnerTelephone() {
    }

    /**
     * Converts a submitted telephone to its E.164 form, or returns {@code null} when the value
     * cannot form a valid E.164 number.
     */
    static String toE164(String value) {
        if (value == null) {
            return null;
        }
        // Strip spaces, dashes and brackets; everything else must be a '+' or a digit.
        String cleaned = value.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            // Already international: keep the '+' and country code.
            digits = cleaned.substring(1);
        }
        else {
            // Assume '+61', dropping a single leading '0' from the national digits.
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        // Require 8 to 15 digits (and nothing but digits) after the '+'.
        if (!digits.matches("[0-9]{8,15}")) {
            return null;
        }
        return "+" + digits;
    }
}
