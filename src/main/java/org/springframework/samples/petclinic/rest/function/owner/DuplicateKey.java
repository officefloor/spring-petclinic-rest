package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Normalises a value for duplicate detection so that comparisons ignore letter
 * case and surrounding or repeated whitespace. Thus {@code "  john   smith "}
 * and {@code "John Smith"} normalise to the same key and count as duplicates.
 */
final class DuplicateKey {

    private DuplicateKey() {
    }

    /**
     * @return a case- and whitespace-insensitive key for {@code value}, or
     *         {@code null} when {@code value} is {@code null} or blank.
     */
    static String of(String value) {
        if (value == null) {
            return null;
        }
        String collapsed = value.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            return null;
        }
        return collapsed.toLowerCase(Locale.ROOT);
    }
}
