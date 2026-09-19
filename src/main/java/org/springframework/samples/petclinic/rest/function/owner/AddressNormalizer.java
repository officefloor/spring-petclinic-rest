package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Shared address normalization: trims and collapses runs of whitespace to a single space,
 * upper-cases, and expands common street-type abbreviations token by token (ST -> STREET,
 * RD -> ROAD, AVE -> AVENUE). Used both to canonicalize an incoming request's address (the
 * stored and returned form) and to compare it against the addresses already held on other
 * owners, so incidental formatting or abbreviation differences do not hide a match.
 */
final class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /**
     * Returns the normalized form of {@code raw}: upper-case, single-spaced tokens with known
     * abbreviations expanded. A {@code null} address yields an empty string, so a value that is
     * blank after normalization is detectable as blank.
     */
    static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        StringBuilder normalized = new StringBuilder();
        for (String token : raw.trim().split("\\s+")) {
            String upper = token.toUpperCase(Locale.ROOT);
            if (normalized.length() > 0) {
                normalized.append(' ');
            }
            normalized.append(ABBREVIATIONS.getOrDefault(upper, upper));
        }
        return normalized.toString();
    }
}
