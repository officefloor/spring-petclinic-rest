package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single source of truth for the owner "E.164 telephone" rule. Both
 * {@link NormalizeOwnerTelephone} (which stores the E.164 value) and
 * {@link OwnerIdentity} (which uses it as the telephone part of the identity key) go through
 * here, so the two can never drift apart.
 *
 * <p>The rule: strip spaces, dashes and brackets; keep a leading {@code '+'} and country
 * code when present, otherwise assume country code {@code '+61'} and drop a single leading
 * {@code '0'} from the national digits; require 8 to 15 digits after the {@code '+'}. So
 * {@code "0412 345 678"} becomes {@code "+61412345678"}.
 *
 * <p>For a recognised country code the national-number length is also fixed: {@code '+61'}
 * requires exactly 9 national digits and {@code '+1'} exactly 10. A number whose country
 * code is known but whose national length is wrong is rejected (returns {@code null}).
 */
final class OwnerTelephones {

    /**
     * Recognised country codes mapped to the exact number of national digits they require.
     * Ordered longest-prefix first so a leading {@code "61"} matches country {@code "61"}
     * before the shorter {@code "1"}.
     */
    private static final Map<String, Integer> NATIONAL_DIGITS = new LinkedHashMap<>();

    static {
        NATIONAL_DIGITS.put("61", 9);
        NATIONAL_DIGITS.put("1", 10);
    }

    private OwnerTelephones() {
    }

    /**
     * Converts {@code telephone} to its E.164 form, or returns {@code null} when it cannot
     * form a valid E.164 number (null input, non-digit content, fewer than 8 / more than
     * 15 digits after the {@code '+'}, or a recognised country code with the wrong
     * national-number length).
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
        // For a recognised country code, the national-number length is fixed.
        for (Map.Entry<String, Integer> country : NATIONAL_DIGITS.entrySet()) {
            String code = country.getKey();
            if (digits.startsWith(code)) {
                int nationalLength = digits.length() - code.length();
                return nationalLength == country.getValue() ? "+" + digits : null;
            }
        }
        return "+" + digits;
    }
}
