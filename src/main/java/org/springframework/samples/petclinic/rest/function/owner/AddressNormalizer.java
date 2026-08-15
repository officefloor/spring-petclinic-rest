package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Normalizes owner addresses to a canonical stored form.
 *
 * <p>Rules: trim, collapse internal whitespace runs to a single space, upper-case, and expand
 * the common abbreviations {@code ST -> STREET}, {@code RD -> ROAD} and {@code AVE -> AVENUE}
 * (whole words only). So {@code "  12  main  st "} becomes {@code "12 MAIN STREET"} and
 * {@code "7 elm ave"} becomes {@code "7 ELM AVENUE"}. An address that is empty or only
 * whitespace normalizes to {@code ""}, which the required-field check rejects.
 *
 * <p>This is the single normalized form every part of the create pipeline uses: it is stored on
 * the owner (and hence returned), and both household duplicate detection and the shared household
 * id compare addresses through it.
 */
public final class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /** Canonical form of {@code input}; {@code ""} for {@code null}/blank input. */
    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        String collapsed = input.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
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
