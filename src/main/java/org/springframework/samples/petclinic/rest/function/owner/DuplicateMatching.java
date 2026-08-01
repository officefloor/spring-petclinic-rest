package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Objects;

/**
 * Shared value matching for owner duplicate detection. Comparisons ignore letter case
 * and surrounding or repeated whitespace, so {@code "  john   smith "} and
 * {@code "John Smith"} are treated as the same value.
 */
final class DuplicateMatching {

    private DuplicateMatching() {
    }

    /**
     * Normalises a value for duplicate comparison: trims, collapses any run of
     * whitespace to a single space and lower-cases. {@code null} stays {@code null}.
     */
    static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String collapsed = value.trim().replaceAll("\\s+", " ");
        return collapsed.toLowerCase(Locale.ROOT);
    }

    /**
     * Whether two values are the same once normalised (case- and whitespace-insensitive).
     */
    static boolean matches(String a, String b) {
        return Objects.equals(normalize(a), normalize(b));
    }
}
