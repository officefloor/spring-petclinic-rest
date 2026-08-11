package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Canonical form for an owner {@code address}, shared by every step that stores or compares
 * one. Normalization trims and collapses internal whitespace runs to a single space,
 * upper-cases, and expands common street-type abbreviations as whole words
 * ({@code ST}&rarr;{@code STREET}, {@code RD}&rarr;{@code ROAD}, {@code AVE}&rarr;{@code AVENUE}).
 * A {@code null} or whitespace-only value normalizes to the empty string.
 *
 * <p>The function is idempotent: normalizing an already-normalized address returns it
 * unchanged, so it is safe to apply again when comparing against stored owners.
 */
final class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS =
            Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    static String normalize(String value) {
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
            String expanded = ABBREVIATIONS.get(tokens[i]);
            sb.append(expanded != null ? expanded : tokens[i]);
        }
        return sb.toString();
    }
}
