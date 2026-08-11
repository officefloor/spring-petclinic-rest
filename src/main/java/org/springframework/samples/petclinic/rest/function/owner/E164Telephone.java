package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Normalises telephone numbers to <a href="https://en.wikipedia.org/wiki/E.164">E.164</a> form.
 *
 * <p>Spaces, dashes and brackets are stripped. A leading {@code '+'} and its country code are kept
 * as given; otherwise country code {@code +61} is assumed and a single leading {@code '0'} is
 * dropped from the national digits. The result is a {@code '+'} followed by 8 to 15 digits, so
 * {@code "0412 345 678"} becomes {@code "+61412345678"}.
 */
final class E164Telephone {

    private E164Telephone() {
    }

    /**
     * Returns the E.164 form of {@code raw}, or {@code null} when it cannot form a valid E.164
     * number (non-digit content after stripping separators, or not 8 to 15 digits after the
     * {@code '+'}).
     */
    static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        // Strip spaces, dashes and brackets.
        String cleaned = raw.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            // Keep the leading '+' and country code as given.
            digits = cleaned.substring(1);
        }
        else {
            // No country code: assume '+61' and drop a single leading '0' from the national digits.
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        return "+" + digits;
    }
}
