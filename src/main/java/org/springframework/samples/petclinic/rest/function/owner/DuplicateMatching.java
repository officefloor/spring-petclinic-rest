package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Objects;

/**
 * Normalisation used by the owner duplicate checks. Two values count as the same when they match
 * after ignoring letter case and any surrounding or repeated whitespace, so {@code "  john   smith "}
 * and {@code "John Smith"} are treated as the same person.
 */
final class DuplicateMatching {

    private DuplicateMatching() {
    }

    /**
     * Canonical form of a value for duplicate comparison: trimmed, internal whitespace runs collapsed
     * to a single space, and lower-cased. Returns {@code null} for a {@code null} input.
     */
    static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Whether two values are equal once normalised for case and whitespace. */
    static boolean sameValue(String a, String b) {
        return Objects.equals(normalize(a), normalize(b));
    }
}
