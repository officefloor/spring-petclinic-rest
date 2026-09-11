package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Normalizes a telephone number to E.164 form.
 *
 * <p>Spaces, dashes and brackets are stripped. A value that already carries a leading
 * {@code '+'} keeps its country code as given; otherwise country code {@code +61} is
 * assumed and a single leading {@code '0'} is dropped from the national digits. The
 * result must contain 8 to 15 digits after the {@code '+'}; anything else is not a
 * valid E.164 number.
 *
 * <p>For example {@code "0412 345 678"} becomes {@code "+61412345678"} and
 * {@code "+64 21 123 456"} becomes {@code "+6421123456"}.
 */
final class TelephoneE164 {

    private TelephoneE164() {
    }

    /**
     * Returns the E.164 form of {@code raw}, or {@code null} when it cannot form a valid
     * E.164 number (including a {@code null} input).
     */
    static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replaceAll("[\\s()\\-]", "");
        if (cleaned.isEmpty()) {
            return null;
        }
        String digits;
        if (cleaned.charAt(0) == '+') {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (digits.isEmpty() || !digits.chars().allMatch(c -> c >= '0' && c <= '9')) {
            return null;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            return null;
        }
        return "+" + digits;
    }
}
