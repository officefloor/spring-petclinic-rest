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

    /** Whether {@code value} carries content once leading/trailing whitespace is ignored. */
    static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * The composed structured address: the {@link #normalize(String) normalized} {@code addressLine1},
     * with a single space and the normalized {@code addressLine2} appended when {@code addressLine2}
     * is present.
     */
    static String compose(String addressLine1, String addressLine2) {
        String composed = normalize(addressLine1);
        if (isPresent(addressLine2)) {
            composed = composed + ' ' + normalize(addressLine2);
        }
        return composed;
    }

    /**
     * The canonical address for an owner: the {@link #compose(String, String) composed} structured
     * address when a structured {@code addressLine1} is present, otherwise the normalized flat
     * {@code address}. Structured fields are preferred so everything downstream reads one form.
     */
    static String canonical(String addressLine1, String addressLine2, String flatAddress) {
        if (isPresent(addressLine1)) {
            return compose(addressLine1, addressLine2);
        }
        return normalize(flatAddress);
    }
}
