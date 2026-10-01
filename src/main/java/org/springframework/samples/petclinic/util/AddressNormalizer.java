package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.Map;

/**
 * Normalizes a free-text postal address into a canonical form so that differently
 * formatted but equivalent addresses are stored, returned and compared consistently.
 *
 * <p>The rules are:
 * <ul>
 *   <li>surrounding whitespace is trimmed and any internal run of whitespace is
 *       collapsed to a single space;</li>
 *   <li>the whole value is upper-cased;</li>
 *   <li>common abbreviations are expanded as whole tokens: {@code ST -> STREET},
 *       {@code RD -> ROAD} and {@code AVE -> AVENUE}.</li>
 * </ul>
 *
 * <p>So {@code '  12  main  st '} becomes {@code '12 MAIN STREET'} and
 * {@code '7 elm ave'} becomes {@code '7 ELM AVENUE'}.
 */
public final class AddressNormalizer {

    /** Whole-token abbreviation expansions, keyed by the already upper-cased token. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /**
     * Normalize a raw address to its canonical form.
     *
     * @param raw the address as supplied by the caller (any format)
     * @return the normalized address, or {@code null} if {@code raw} is {@code null}.
     *         A value consisting only of whitespace normalizes to an empty string.
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String collapsed = raw.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return collapsed;
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
