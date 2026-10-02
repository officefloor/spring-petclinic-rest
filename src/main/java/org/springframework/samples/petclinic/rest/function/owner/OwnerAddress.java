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
