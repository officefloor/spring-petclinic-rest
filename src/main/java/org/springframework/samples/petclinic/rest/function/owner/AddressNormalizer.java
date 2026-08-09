package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Canonical form for an owner {@code address}, so that a value is stored, returned and compared in a
 * single consistent shape. Normalization: trim, collapse internal runs of whitespace to a single
 * space, upper-case, and expand common street-type abbreviations token-by-token ({@code ST} →
 * {@code STREET}, {@code RD} → {@code ROAD}, {@code AVE} → {@code AVENUE}).
 *
 * <p>Expansion matches only a whole space-delimited token, so {@code "st"} in {@code "12 main st"}
 * expands but {@code "st."} (with a trailing period) and substrings such as the {@code "st"} in
 * {@code "castle"} do not. A {@code null} or whitespace-only input normalizes to the empty string,
 * which the required-field check ({@link ValidateOwner}) treats as blank.
 */
public final class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /** Normalize an address to its canonical stored/compared form; {@code ""} when blank or null. */
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
