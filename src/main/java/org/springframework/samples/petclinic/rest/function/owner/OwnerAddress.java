package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Single source of truth for what a "normalized" owner address is. Both {@link ValidateNewOwner}
 * (which enforces the rule and stores the normalized value) and {@link Household} (which compares
 * addresses for duplicate detection and the shared household id) normalize through here, so the
 * duplicate check, the stored/returned value and the household id can never drift apart.
 *
 * <p>Normalization:
 * <ul>
 *   <li>trims and collapses runs of whitespace to a single space;</li>
 *   <li>upper-cases (locale-independent);</li>
 *   <li>expands common abbreviations as whole words: {@code ST -> STREET}, {@code RD -> ROAD},
 *       {@code AVE -> AVENUE}.</li>
 * </ul>
 * A {@code null} or all-whitespace input normalizes to the empty string, which callers treat as a
 * blank (and therefore missing) address.
 */
final class OwnerAddress {

    /** Whole-word abbreviations expanded during normalization (keys are already upper-cased). */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private OwnerAddress() {
    }

    /**
     * The canonical stored form of a single structured address line: the {@link #normalize(String)
     * normalized} value, or {@code null} when it is absent/blank. This is the one place the
     * "blank line is stored as null" policy lives, so both the create ({@link ValidateNewOwner}) and
     * update ({@link ApplyOwner}) paths canonicalize a line identically.
     */
    static String normalizeToNull(String line) {
        String normalized = normalize(line);
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Composes the returned/stored address from the two normalized structured lines: the normalized
     * {@code line1} with the normalized {@code line2} appended after a single space when {@code line2}
     * is present (non-blank). Inputs are normalized through {@link #normalize(String)} first, so callers
     * may pass the raw request values. Returns the empty string when {@code line1} is blank.
     */
    private static String compose(String line1, String line2) {
        String l1 = normalize(line1);
        String l2 = normalize(line2);
        if (l1.isEmpty()) {
            return "";
        }
        return l2.isEmpty() ? l1 : l1 + " " + l2;
    }

    /**
     * The effective, normalized address for an owner, preferring the structured form: the
     * {@link #compose(String, String) composed} structured lines when {@code line1} is present,
     * otherwise the normalized flat {@code address}. The single source of truth for what the stored and
     * returned {@code address} is and for the "address supplied in either form" validity rule — the
     * empty string means no address was supplied in either form.
     */
    static String effective(String line1, String line2, String address) {
        String composed = compose(line1, line2);
        return composed.isEmpty() ? normalize(address) : composed;
    }

    /**
     * Returns the normalized form of {@code address}, or the empty string when it is {@code null} or
     * blank after trimming.
     */
    static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
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
