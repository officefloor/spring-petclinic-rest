package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Converts a raw telephone into E.164 form. A leading '+' with its country code is kept; otherwise
 * country code '+61' is assumed and a single leading '0' is dropped from the national digits. Spaces,
 * dashes and brackets are stripped. Returns {@code null} when the result is not 8 to 15 digits.
 */
public final class E164 {

    private E164() {
    }

    public static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        boolean explicit = raw.trim().startsWith("+");
        String digits = raw.replaceAll("\\D", "");
        if (!explicit) {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            digits = "61" + digits;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            return null;
        }
        return "+" + digits;
    }
}
