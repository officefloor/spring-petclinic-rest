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

    /**
     * Normalize a single address line, returning {@code null} (rather than an empty string) for a
     * {@code null} or blank value — so an absent structured line is stored as {@code null}.
     */
    static String normalizeToNull(String value) {
        String normalized = normalize(value);
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Compose the effective address, preferring the structured fields when present. When
     * {@code addressLine1} is non-blank the result is its normalized form, with a single space and
     * the normalized {@code addressLine2} appended when that line is present; otherwise it falls back
     * to the normalized flat {@code address}. Returns an empty string when none is supplied.
     */
    static String compose(String addressLine1, String addressLine2, String address) {
        String line1 = normalize(addressLine1);
        if (!line1.isEmpty()) {
            String line2 = normalize(addressLine2);
            return line2.isEmpty() ? line1 : line1 + " " + line2;
        }
        return normalize(address);
    }
}
