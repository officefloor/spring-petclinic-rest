package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Converts a telephone number to E.164 form. A leading {@code '+'} and country code are kept when
 * present; otherwise country code {@code '+61'} is assumed and a single leading {@code '0'} is dropped
 * from the national digits. Spaces, dashes and brackets are stripped. The result must have 8 to 15
 * digits after the {@code '+'}, otherwise it cannot form valid E.164.
 */
final class TelephoneE164 {

    private TelephoneE164() {
    }

    /**
     * Normalizes {@code raw} to E.164, throwing {@link InvalidTelephoneException} when it cannot.
     */
    static String normalize(String raw) throws InvalidTelephoneException {
        String e164 = normalizeOrNull(raw);
        if (e164 == null) {
            throw new InvalidTelephoneException(raw);
        }
        return e164;
    }

    /**
     * Normalizes {@code raw} to E.164, returning {@code null} when it cannot form valid E.164.
     */
    static String normalizeOrNull(String raw) {
        if (raw == null) {
            return null;
        }
        // Strip spaces, dashes and brackets, keeping a leading '+' and the digits.
        String cleaned = raw.trim().replaceAll("[\\s()\\-]", "");
        String national;
        if (cleaned.startsWith("+")) {
            national = cleaned.substring(1);
            if (!national.matches("\\d+")) {
                return null;
            }
        }
        else {
            if (!cleaned.matches("\\d+")) {
                return null;
            }
            // No country code: assume +61 and drop a single leading '0' from the national digits.
            if (cleaned.startsWith("0")) {
                cleaned = cleaned.substring(1);
            }
            national = "61" + cleaned;
        }
        if (national.length() < 8 || national.length() > 15) {
            return null;
        }
        return "+" + national;
    }
}
