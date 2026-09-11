package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Normalizes a street address to a canonical form.
 *
 * <p>Leading and trailing whitespace is trimmed and internal whitespace runs are
 * collapsed to a single space; the result is upper-cased; and common street-type
 * abbreviations are expanded token-wise ({@code ST -> STREET}, {@code RD -> ROAD},
 * {@code AVE -> AVENUE}).
 *
 * <p>For example {@code "  12  main  st "} becomes {@code "12 MAIN STREET"} and
 * {@code "7 elm ave"} becomes {@code "7 ELM AVENUE"}. An address that is empty or all
 * whitespace (or {@code null}) normalizes to the empty string.
 */
final class AddressNormalizer {

    /** Abbreviation (upper-cased) -> expanded street type. */
    private static final Map<String, String> ABBREVIATIONS =
            Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /**
     * Returns the normalized form of {@code raw}, or the empty string when it is
     * {@code null} or blank after trimming.
     */
    static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String collapsed = raw.trim().replaceAll("\\s+", " ").toUpperCase();
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            String expanded = ABBREVIATIONS.get(tokens[i]);
            if (expanded != null) {
                tokens[i] = expanded;
            }
        }
        return String.join(" ", tokens);
    }
}
