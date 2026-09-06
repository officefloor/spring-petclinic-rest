package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Shared handling for the owner {@code address}. Addresses are stored in a normalized form:
 * surrounding whitespace is trimmed, runs of internal whitespace collapse to a single space,
 * the text is upper-cased and common street-type abbreviations are expanded
 * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}).
 *
 * <p>So {@code '  12  main  st '} is stored as {@code '12 MAIN STREET'}.
 */
final class Addresses {

    // Common street-type abbreviations expanded to their full word (compared upper-cased).
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private Addresses() {
    }

    /**
     * Normalize an address: trim, collapse whitespace, upper-case and expand common
     * abbreviations. Returns an empty string for a {@code null} or blank value.
     */
    static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
    }
}
