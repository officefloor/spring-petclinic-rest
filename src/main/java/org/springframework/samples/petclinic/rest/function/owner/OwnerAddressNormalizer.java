package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Canonical address normalization applied whenever an owner is created. The rules are: trim the
 * ends, collapse every run of whitespace to a single space, upper-case, and expand a small set of
 * common street-type abbreviations to their full word ({@code ST -> STREET}, {@code RD -> ROAD},
 * {@code AVE -> AVENUE}). The result is what gets stored and returned, and it is the form every
 * address comparison (household duplicate detection and the shared household id) uses, so equal
 * addresses written differently collapse to the same value.
 */
final class OwnerAddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private OwnerAddressNormalizer() {
    }

    /** Normalize an address; a null or whitespace-only value normalizes to the empty string. */
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
            sb.append(ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
    }
}
