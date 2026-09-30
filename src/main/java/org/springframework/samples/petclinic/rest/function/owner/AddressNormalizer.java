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
