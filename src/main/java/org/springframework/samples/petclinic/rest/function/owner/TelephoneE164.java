package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Converts a raw telephone string to E.164 form: keep a leading '+' and country code
 * when present, otherwise assume country code '+61' and drop a single leading '0' from
 * the national digits. Spaces, dashes and brackets are stripped, and the result must
 * carry 8 to 15 digits after the '+'. For recognised country codes the national-number
 * length is checked exactly ('+61' requires 9 national digits, '+1' requires 10); a
 * wrong length is rejected. Returns {@code null} when no valid E.164 number can be
 * formed, so callers reject with a 400.
 */
final class TelephoneE164 {

    /** E.164: a '+' followed by 8 to 15 digits. */
    private static final Pattern E164 = Pattern.compile("\\d{8,15}");

    /**
     * Required national-number length per country code. No key is a prefix of another
     * (the recognised codes '61' and '1' do not overlap), so prefix matching is
     * unambiguous regardless of iteration order.
     */
    private static final Map<String, Integer> NATIONAL_LENGTHS = Map.of(
            "61", 9,
            "1", 10);

    private TelephoneE164() {
    }

    static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        // Strip spaces, dashes and brackets; a leading '+' (after trimming) is significant.
        String stripped = raw.trim().replaceAll("[\\s\\-()]", "");
        String digits;
        if (stripped.startsWith("+")) {
            // Keep the caller's country code as-is.
            digits = stripped.substring(1);
        }
        else {
            // National number: assume '+61' and drop a single leading '0'.
            String national = stripped.startsWith("0") ? stripped.substring(1) : stripped;
            digits = "61" + national;
        }
        if (!E164.matcher(digits).matches()) {
            return null;
        }
        // For a recognised country code, the national number must be exactly the right
        // length; other codes keep only the general 8-to-15-digit rule above.
        for (Map.Entry<String, Integer> code : NATIONAL_LENGTHS.entrySet()) {
            if (digits.startsWith(code.getKey())) {
                int nationalLength = digits.length() - code.getKey().length();
                if (nationalLength != code.getValue()) {
                    return null;
                }
                break;
            }
        }
        return "+" + digits;
    }
}
