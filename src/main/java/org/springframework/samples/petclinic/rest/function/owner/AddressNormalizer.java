package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Shared address normalization for created owners: trims and collapses whitespace, upper-cases,
 * and expands common street-type abbreviations ({@code ST->STREET}, {@code RD->ROAD},
 * {@code AVE->AVENUE}). Used to store and return an owner's address in canonical form (see
 * {@link NormalizeOwnerAddress}), to reject an address that is blank once normalized
 * ({@link ValidateRequiredOwnerFields}) and to compare addresses for household identity
 * ({@link Household}).
 */
final class AddressNormalizer {

    /** Street-type abbreviations expanded to their full word, keyed by their upper-cased token. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /**
     * The normalized form of an address: leading/trailing and repeated inner whitespace collapsed to
     * a single space, upper-cased, and each known street-type abbreviation expanded. A {@code null}
     * or all-whitespace address normalizes to the empty string.
     */
    static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder normalized = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                normalized.append(' ');
            }
            normalized.append(ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return normalized.toString();
    }
}
