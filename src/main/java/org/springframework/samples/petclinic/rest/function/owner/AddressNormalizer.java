package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Canonical form for an owner's postal address, applied on create. Trims and collapses
 * runs of whitespace to a single space, upper-cases, and expands common street-type
 * abbreviations ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}) as whole
 * tokens (a single trailing {@code '.'} is ignored, so {@code "St."} also expands). So
 * {@code "  12  main  st "} becomes {@code "12 MAIN STREET"}.
 *
 * <p>Every comparison of addresses (household duplicate detection and the shared household
 * id) canonicalizes both sides through {@link #normalize(String)} so they use the same
 * normalized form. Returns {@code null} when the value is blank after normalization.
 */
final class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /**
     * Normalize an address to its canonical stored form, or {@code null} when the value is
     * {@code null} or blank once trimmed.
     */
    static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String collapsed = value.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            return null;
        }
        String[] tokens = collapsed.toUpperCase(Locale.ROOT).split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            String key = token.endsWith(".") ? token.substring(0, token.length() - 1) : token;
            String expanded = ABBREVIATIONS.get(key);
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(expanded != null ? expanded : token);
        }
        return sb.toString();
    }

    /**
     * Compose the stored/returned address from already-normalized structured lines: the
     * normalized {@code line1}, with a single space and the normalized {@code line2}
     * appended when {@code line2} is present. Returns {@code null} when {@code line1} is
     * {@code null} (no structured address).
     */
    static String compose(String line1, String line2) {
        if (line1 == null) {
            return null;
        }
        return line2 == null ? line1 : line1 + " " + line2;
    }
}
