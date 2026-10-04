package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Converts a raw, user-supplied postal address into a canonical form for storage.
 *
 * <p>The rules are:
 * <ul>
 *     <li>the value is trimmed and every run of whitespace is collapsed to a single space;</li>
 *     <li>the result is upper-cased;</li>
 *     <li>common street-type abbreviations are expanded as whole tokens
 *         ({@code ST->STREET}, {@code RD->ROAD}, {@code AVE->AVENUE}).</li>
 * </ul>
 *
 * <p>So {@code "  12  main  st "} becomes {@code "12 MAIN STREET"} and {@code "7 elm ave"}
 * becomes {@code "7 ELM AVENUE"}. The normalization is idempotent: feeding it an already
 * normalized value returns that same value.
 */
public final class AddressNormalizer {

    /**
     * Known street-type abbreviations expanded during normalization. Keyed by the upper-cased
     * token, so the lookup runs after the address has been upper-cased.
     */
    private static final Map<String, String> STREET_ABBREVIATIONS = Map.of(
        "ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /**
     * Normalize a raw postal address to its canonical form, or return {@code null} when the
     * input is {@code null}. A value that is blank once trimmed normalizes to an empty string.
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String collapsed = raw.trim().replaceAll("\\s+", " ").toUpperCase();
        if (collapsed.isEmpty()) {
            return collapsed;
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(STREET_ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
    }
}
