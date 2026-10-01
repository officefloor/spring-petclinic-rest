package org.springframework.samples.petclinic.model;

/**
 * Single source of truth for the "E.164 telephone" concept: the validating step converts a submitted
 * value to E.164 before it is stored, and the derived {@link OwnerIdentity} normalizes stored values
 * the same way when forming an owner's identity key. Keeping the rule here ensures the value that gets
 * stored and the value compared for identity can never silently diverge.
 */
public final class OwnerTelephone {

    private OwnerTelephone() {
    }

    /**
     * Convert a raw telephone to E.164 form, or return {@code null} if it cannot form a valid E.164
     * number. Spaces, dashes and brackets are stripped. A leading {@code '+'} keeps its country code;
     * otherwise country code {@code '+61'} is assumed and a single leading {@code '0'} is dropped from
     * the national digits. The result must have 8 to 15 digits after the {@code '+'}.
     *
     * <p>The national-number length is also checked against the country code: {@code '+61'} (Australia)
     * requires 9 national digits and {@code '+1'} (NANP) requires 10. A number whose national part is
     * the wrong length for its country code cannot form a valid E.164 number and returns {@code null}.
     */
    public static String toE164(String value) {
        if (value == null) {
            return null;
        }
        // Strip spaces, dashes and brackets (round and square).
        String cleaned = value.replaceAll("[\\s()\\[\\]-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            return null;
        }
        if (!nationalLengthValid(digits)) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Check the national-number length for the known country codes. {@code '+61'} requires 9 national
     * digits and {@code '+1'} requires 10; other country codes are not length-checked here.
     */
    private static boolean nationalLengthValid(String digits) {
        if (digits.startsWith("61")) {
            return digits.length() - 2 == 9;
        }
        if (digits.startsWith("1")) {
            return digits.length() - 1 == 10;
        }
        return true;
    }
}
