package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Shared address handling: canonicalizes a free-text street address so equivalent
 * addresses written with different casing, spacing or common abbreviations are stored
 * and compared uniformly. Used both to normalize an owner request before it is persisted
 * and to compare addresses when detecting shared households (see {@link Households}).
 */
final class Addresses {

    /** Common street-type abbreviations expanded to their full word (whole words only). */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private Addresses() {
    }

    /**
     * Returns the canonical form of {@code address}: trimmed, with every run of
     * whitespace collapsed to a single space, upper-cased, and common street-type
     * abbreviations ({@code ST}, {@code RD}, {@code AVE}) expanded to their full words.
     * Returns {@code null} when {@code address} is {@code null}.
     */
    static String normalize(String address) {
        if (address == null) {
            return null;
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return collapsed;
        }
        String[] words = collapsed.split(" ");
        StringBuilder result = new StringBuilder(collapsed.length());
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(' ');
            }
            result.append(ABBREVIATIONS.getOrDefault(words[i], words[i]));
        }
        return result.toString();
    }
}
