package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single source of truth for the owner-telephone E.164 rule. Both {@link ValidateOwnerFields}
 * (which normalizes the submitted value) and {@link OwnerIdentity} (which builds the identity key
 * compared against stored owners) convert through {@link #toE164} so the two can never drift apart.
 *
 * <p>The rule: strip spaces, dashes and brackets. If what remains starts with a '+', keep that
 * leading '+' and country code as-is. Otherwise assume Australian country code '+61', dropping a
 * single leading '0' from the national digits before prefixing. The result must be a '+' followed
 * by 8 to 15 digits; anything else cannot form valid E.164 and yields {@code null}.
 *
 * <p>The national-number length is then checked against the country calling code: '+61' requires
 * exactly 9 national digits and '+1' requires exactly 10. A number with the wrong national length
 * for its country code yields {@code null} (rejected as an invalid telephone). Country codes other
 * than these keep only the generic 8-to-15-digit bound.
 *
 * <p>Example: {@code "0412 345 678"} becomes {@code "+61412345678"}.
 */
public final class OwnerTelephone {

    /**
     * Country calling code to required national-number length. Iteration order is longest code
     * first so a longer code is matched before a shorter one that is a prefix of it.
     */
    private static final Map<String, Integer> NATIONAL_LENGTH = new LinkedHashMap<>();

    static {
        NATIONAL_LENGTH.put("61", 9);
        NATIONAL_LENGTH.put("1", 10);
    }

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
        // Check the national-number length against the country calling code.
        for (Map.Entry<String, Integer> entry : NATIONAL_LENGTH.entrySet()) {
            String code = entry.getKey();
            if (digits.startsWith(code)) {
                int nationalLength = digits.length() - code.length();
                return nationalLength == entry.getValue() ? "+" + digits : null;
            }
        }
        return "+" + digits;
    }

    /**
     * Formats a stored E.164 telephone for humans as the country calling code, a space, then the
     * national digits grouped in threes from the left (e.g. {@code "+61412345678"} becomes
     * {@code "+61 412 345 678"}). The country calling code is identified from the known codes in
     * {@link #NATIONAL_LENGTH}; an unrecognized or non-E.164 value is returned unchanged.
     */
    public static String toDisplay(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        for (String code : NATIONAL_LENGTH.keySet()) {
            if (digits.startsWith(code)) {
                return "+" + code + " " + groupInThrees(digits.substring(code.length()));
            }
        }
        return e164;
    }

    /** Groups a run of digits into space-separated groups of three, from the left. */
    private static String groupInThrees(String national) {
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return grouped.toString();
    }
}
