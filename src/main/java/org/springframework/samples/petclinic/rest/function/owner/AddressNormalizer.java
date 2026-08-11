package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Normalises a free-text owner {@code address} to a single canonical form.
 *
 * <p>Whitespace is trimmed and internal runs collapse to a single space, the text is upper-cased,
 * and common street-type abbreviations are expanded on whole tokens ({@code ST -> STREET},
 * {@code RD -> ROAD}, {@code AVE -> AVENUE}). So {@code "  12  main  st "} becomes
 * {@code "12 MAIN STREET"}. A {@code null} or whitespace-only value normalises to the empty string.
 */
final class AddressNormalizer {

    private AddressNormalizer() {
    }

    /**
     * Returns the canonical form of {@code raw}, or {@code ""} when it is {@code null} or blank once
     * whitespace is trimmed. The result is idempotent: normalising an already-normalised address
     * returns it unchanged.
     */
    static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (cleaned.isEmpty()) {
            return "";
        }
        String[] tokens = cleaned.split(" ");
        StringBuilder sb = new StringBuilder(cleaned.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(expand(tokens[i]));
        }
        return sb.toString();
    }

    /** Expands a single upper-cased token if it is a known street-type abbreviation. */
    private static String expand(String token) {
        switch (token) {
            case "ST":
                return "STREET";
            case "RD":
                return "ROAD";
            case "AVE":
                return "AVENUE";
            default:
                return token;
        }
    }
}
