package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Normalises values used by the owner duplicate checks so that comparisons ignore
 * letter case and surrounding or repeated whitespace. For example
 * {@code "  john   smith "} and {@code "John Smith"} normalise to the same key and
 * so count as the same value when detecting duplicates.
 */
final class DuplicateKey {

    private DuplicateKey() {
    }

    /**
     * @return the normalised comparison key, or {@code null} when {@code value} is
     *         {@code null} or contains only whitespace.
     */
    static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String collapsed = value.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            return null;
        }
        return collapsed.toLowerCase(Locale.ROOT);
    }

    /**
     * Normalises a telephone number to digits only, stripping spaces, dashes,
     * parentheses and any other non-digit characters. This is both how a
     * telephone is stored and the comparison key for the uniqueness check, so
     * {@code "(613) 555-0100"} and {@code "6135550100"} are treated as the same
     * number.
     *
     * @return the digits-only value, or {@code null} when {@code value} is
     *         {@code null} or contains no digits.
     */
    static String telephone(String value) {
        if (value == null) {
            return null;
        }
        String digits = value.replaceAll("\\D", "");
        return digits.isEmpty() ? null : digits;
    }
}
