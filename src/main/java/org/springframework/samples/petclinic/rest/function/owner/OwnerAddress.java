package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Shared address normalization for the owner create pipeline. An address is normalized by
 * trimming and collapsing internal runs of whitespace to a single space, upper-casing, and
 * expanding common street-type abbreviations token-by-token ({@code ST -> STREET},
 * {@code RD -> ROAD}, {@code AVE -> AVENUE}). So {@code "  12  main  st "} becomes
 * {@code "12 MAIN STREET"} and {@code "7 elm ave"} becomes {@code "7 ELM AVENUE"}.
 *
 * <p>The normalized value is what {@code POST /api/owners} stores and returns as {@code address},
 * and it is the form every address comparison (household duplicate detection and the shared
 * household id) is done against. Normalization is idempotent: normalizing an already-normalized
 * address yields the same value.
 */
final class OwnerAddress {

    /** Whole-token abbreviations expanded to their full street type. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private OwnerAddress() {
    }

    /**
     * Normalizes an address. A {@code null} input is left as {@code null}; an input that is empty
     * or only whitespace normalizes to {@code ""} (which the required-field check treats as blank).
     */
    static String normalize(String input) {
        if (input == null) {
            return null;
        }
        String collapsed = input.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.toUpperCase(Locale.ROOT).split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
    }

    /** The normalized address, with {@code null} mapped to {@code ""} for safe comparison. */
    static String normalizeForCompare(String input) {
        String normalized = normalize(input);
        return normalized == null ? "" : normalized;
    }
}
