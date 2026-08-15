package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Shared normalization for an owner's {@code address}, applied whenever an owner is created.
 * Leading and trailing whitespace is trimmed and every internal run of whitespace is collapsed
 * to a single space; the result is upper-cased; and common street-type abbreviations are expanded
 * as whole words: {@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}. So {@code
 * "  12  main  st "} becomes {@code "12 MAIN STREET"}.
 *
 * <p>The normalized value is what is stored and returned, what the required-field check tests for
 * blankness, and the form every address comparison uses (household duplicate detection and the
 * shared household id).
 */
final class OwnerAddress {

    /** Any run of whitespace, collapsed to a single space. */
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    /** Whole-word abbreviations expanded to their canonical street type (keys are upper-case). */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private OwnerAddress() {
    }

    /**
     * Returns the normalized address for the given raw value, or {@code ""} when {@code null}.
     * A value that is empty or only whitespace normalizes to {@code ""}.
     */
    static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = WHITESPACE.matcher(address.trim()).replaceAll(" ");
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.toUpperCase().split(" ");
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
