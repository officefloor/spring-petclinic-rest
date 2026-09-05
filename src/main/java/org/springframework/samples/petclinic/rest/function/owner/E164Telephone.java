package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Converts a raw telephone number to E.164 form: strip spaces, dashes and brackets; keep
 * a leading '+' and country code when present, otherwise assume country code '+61' and
 * drop a single leading '0' from the national digits. Requires 8 to 15 digits after the
 * '+'. Returns {@code null} when no valid E.164 number can be formed.
 */
public final class E164Telephone {

    private E164Telephone() {
    }

    public static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            if (cleaned.startsWith("0")) {
                cleaned = cleaned.substring(1);
            }
            digits = "61" + cleaned;
        }
        return digits.matches("\\d{8,15}") ? "+" + digits : null;
    }
}
