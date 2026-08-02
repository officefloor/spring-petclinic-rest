package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Normalises the string fields used by the owner duplicate checks so that comparisons ignore
 * letter case and surrounding or repeated whitespace. For example {@code "  john   smith "} and
 * {@code "John Smith"} normalise to the same key and therefore count as the same person.
 */
final class DuplicateKey {

    private DuplicateKey() {
    }

    /**
     * Returns a comparison key for the given value: leading/trailing whitespace removed, any run
     * of internal whitespace collapsed to a single space, and letters lower-cased. Returns
     * {@code null} when the value is {@code null} so callers can treat "no value" distinctly.
     */
    static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
