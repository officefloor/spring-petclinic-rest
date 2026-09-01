package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Converts a raw telephone into E.164 form: keeps a leading '+' and country code when present,
 * otherwise assumes country code '+61' and drops a single leading '0' from the national digits.
 * Spaces, dashes and brackets are stripped. Returns {@code null} when the result would not have
 * 8 to 15 digits after the '+', or when the national number's length does not match its country
 * code ('+61' requires 9 national digits, '+1' requires 10).
 */
final class E164Telephone {

    private E164Telephone() {
    }

    static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        boolean international = raw.trim().startsWith("+");
        String digits = raw.replaceAll("\\D", "");
        if (!international) {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            digits = "61" + digits;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            return null;
        }
        if (digits.startsWith("61") && digits.length() != 2 + 9) {
            return null;
        }
        if (digits.startsWith("1") && digits.length() != 1 + 10) {
            return null;
        }
        return "+" + digits;
    }
}
