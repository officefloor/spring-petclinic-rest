package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Single source of truth for what a "normalized" owner address is. Used by
 * {@link NormalizeOwnerAddress} when storing and returning the value, and by
 * {@link Households} when comparing addresses for household duplicate detection and the
 * shared household id, so the rule cannot drift between storing and comparing.
 *
 * <p>The rule: the value is trimmed, runs of whitespace are collapsed to a single space
 * and the whole value is upper-cased; then each whitespace-separated token that is a
 * common street-type abbreviation is expanded to its full form ({@code ST -> STREET},
 * {@code RD -> ROAD}, {@code AVE -> AVENUE}). The result is the canonical address;
 * it is empty when the input was blank (only whitespace).
 */
final class OwnerAddresses {

    /** Common street-type abbreviations expanded to their full upper-case form. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private OwnerAddresses() {
    }

    /**
     * Returns the normalized form of {@code address}: trimmed, whitespace collapsed,
     * upper-cased and with common abbreviations expanded. Returns {@code null} for a
     * {@code null} input and an empty string for an input that is blank after trimming.
     */
    static String normalize(String address) {
        if (address == null) {
            return null;
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
     * Composes the canonical stored address from the structured lines, both already
     * {@link #normalize(String) normalized}: the normalized {@code addressLine1}, with a
     * single space and the normalized {@code addressLine2} appended when a line 2 is
     * present. Pass a {@code null} or empty {@code normalizedLine2} when no second line
     * was supplied, in which case the composed address is just line 1.
     */
    static String compose(String normalizedLine1, String normalizedLine2) {
        if (normalizedLine2 == null || normalizedLine2.isEmpty()) {
            return normalizedLine1;
        }
        return normalizedLine1 + " " + normalizedLine2;
    }
}
