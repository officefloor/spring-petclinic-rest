package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Normalizes owner field values for duplicate detection so that differences in
 * letter case and in surrounding or repeated whitespace are ignored. For example
 * {@code "  john   smith "} and {@code "John Smith"} normalize to the same value
 * and therefore count as the same person when detecting duplicates.
 */
final class OwnerFieldNormalizer {

    private OwnerFieldNormalizer() {
    }

    /**
     * Returns a comparison key for the given value: surrounding whitespace removed,
     * internal whitespace runs collapsed to a single space, and letters lower-cased.
     * {@code null} in yields {@code null} out.
     */
    static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes a telephone number to digits only, stripping spaces, dashes and
     * parentheses. This is both the value stored for the number and the comparison
     * key for telephone-uniqueness, so {@code "(613) 555-0100"} and {@code "6135550100"}
     * are treated as the same number. {@code null} in yields {@code null} out.
     */
    static String normalizeTelephone(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("[\\s()-]", "");
    }
}
