package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Optional;

/**
 * Shared telephone handling: converts a raw number to its canonical E.164 form so numbers
 * written with different separators (or an explicit country code) compare equal. Used both
 * to normalize a create request before it is persisted and to compare it against existing
 * owners' stored numbers.
 */
final class Telephones {

    /** Country code assumed when the input carries no explicit '+' prefix (Australia). */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    private Telephones() {
    }

    /**
     * Converts {@code telephone} to E.164 (a leading '+' followed by 8 to 15 digits).
     *
     * <p>Spaces, dashes and brackets are stripped. When the number keeps a leading '+' its
     * country code is preserved; otherwise {@code +61} is assumed and a single leading '0'
     * is dropped from the national digits. Returns empty when the result cannot form valid
     * E.164 (non-digits remain, or the digit count is out of range).
     */
    static Optional<String> toE164(String telephone) {
        if (telephone == null) {
            return Optional.empty();
        }
        String cleaned = telephone.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = DEFAULT_COUNTRY_CODE + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            return Optional.empty();
        }
        return Optional.of("+" + digits);
    }
}
