package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Canonical form for an owner address, applied whenever an owner is created. Trims and collapses
 * runs of whitespace to a single space, upper-cases, and expands common abbreviations
 * ({@code ST}&nbsp;&rarr;&nbsp;{@code STREET}, {@code RD}&nbsp;&rarr;&nbsp;{@code ROAD},
 * {@code AVE}&nbsp;&rarr;&nbsp;{@code AVENUE}) as whole words. A {@code null} or blank value
 * normalizes to the empty string. The result is idempotent, so an already-normalized address is
 * returned unchanged. This is the single source of truth every address comparison uses.
 */
public final class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /** Normalize an address to its canonical stored/returned form. */
    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
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
