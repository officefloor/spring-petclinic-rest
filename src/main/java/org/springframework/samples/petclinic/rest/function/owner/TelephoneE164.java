package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

/**
 * Converts a raw telephone string to E.164 form: keep a leading '+' and country code
 * when present, otherwise assume country code '+61' and drop a single leading '0' from
 * the national digits. Spaces, dashes and brackets are stripped, and the result must
 * carry 8 to 15 digits after the '+'. Returns {@code null} when no valid E.164 number
 * can be formed, so callers reject with a 400.
 */
final class TelephoneE164 {

    /** E.164: a '+' followed by 8 to 15 digits. */
    private static final Pattern E164 = Pattern.compile("\\d{8,15}");

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
        return "+" + digits;
    }
}
