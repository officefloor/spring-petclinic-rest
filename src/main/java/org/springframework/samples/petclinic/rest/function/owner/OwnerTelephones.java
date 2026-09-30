package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single definition of how an owner telephone is converted to E.164 form: strip spaces,
 * dashes and brackets; keep a leading '+' and country code when present, otherwise assume
 * country code '+61' and drop a single leading '0' from the national digits; require 8 to
 * 15 digits after the '+', and additionally require the national-number length to match the
 * country code ('+61' => 9 national digits, '+1' => 10). Both {@link NormalizeOwnerTelephone} (which normalizes the
 * incoming request) and {@link EnsureUniqueOwnerTelephone} (which normalizes stored owners
 * for comparison) call this, so the create-time rule and the dedup comparison can never
 * silently diverge.
 */
final class OwnerTelephones {

    /**
     * Required national-number length per country calling code. Iterated in order so the
     * longest matching code wins when one code is a prefix of another; the current two
     * codes ('61', '1') are not prefixes of each other, so order is immaterial here.
     */
    private static final Map<String, Integer> NATIONAL_LENGTH_BY_CODE = new LinkedHashMap<>();
    static {
        NATIONAL_LENGTH_BY_CODE.put("61", 9);
        NATIONAL_LENGTH_BY_CODE.put("1", 10);
    }

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
        // Enforce the national-number length for known country codes ('+61' => 9, '+1' => 10);
        // any other code keeps only the generic 8-to-15-digit rule above.
        for (Map.Entry<String, Integer> entry : NATIONAL_LENGTH_BY_CODE.entrySet()) {
            String code = entry.getKey();
            if (digits.startsWith(code)) {
                if (digits.length() - code.length() != entry.getValue()) {
                    return null;
                }
                break;
            }
        }
        return "+" + digits;
    }
}
