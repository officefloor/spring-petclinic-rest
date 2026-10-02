package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Canonical address normalization for the owner-create pipeline. Applied once when an owner is
 * created so the stored and returned {@code address} is canonical, and reused for every address
 * comparison (household duplicate detection in {@link Household} and the shared household id) so
 * all steps compare the same form.
 *
 * <p>Normalization: trim, collapse any run of whitespace to a single space, upper-case, and
 * expand common abbreviations as whole words — {@code ST -> STREET}, {@code RD -> ROAD},
 * {@code AVE -> AVENUE}. Yields {@code ""} for a {@code null} or whitespace-only input.
 */
final class OwnerAddress {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private OwnerAddress() {
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
            sb.append(ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
    }
}
