package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * The single definition of address normalization, applied when an owner is created
 * (see {@link NormalizeOwnerAddress}) so that the stored and returned address is in one
 * canonical form.
 *
 * <p>Normalization trims and collapses whitespace runs to a single space, upper-cases the
 * value, and expands the common street-type abbreviations {@code ST->STREET},
 * {@code RD->ROAD} and {@code AVE->AVENUE} (matched as whole space-separated tokens).
 */
final class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /**
     * Compose the flat address from the structured lines: {@code line1}, with a single space
     * and {@code line2} appended when {@code line2} is non-blank. A blank {@code line2} yields
     * {@code line1} alone.
     */
    static String compose(String line1, String line2) {
        if (line2 == null || line2.isBlank()) {
            return line1;
        }
        return line1 + " " + line2;
    }

    /**
     * The single definition of the "prefer structured" address-selection rule: the effective
     * flat address is the {@link #compose composed} structured value when {@code line1} is
     * non-blank, otherwise the flat {@code flat} value. Each argument is used as given, so
     * callers pass normalized or raw values as their step requires.
     */
    static String effectiveAddress(String line1, String line2, String flat) {
        if (line1 != null && !line1.isBlank()) {
            return compose(line1, line2);
        }
        return flat;
    }

    /**
     * Normalize an address to its canonical stored/compared form. A {@code null} or
     * blank input yields the empty string.
     */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder result = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].toUpperCase(Locale.ROOT);
            token = ABBREVIATIONS.getOrDefault(token, token);
            if (i > 0) {
                result.append(' ');
            }
            result.append(token);
        }
        return result.toString();
    }
}
