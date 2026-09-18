package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

import org.springframework.util.StringUtils;

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

    /**
     * The single canonical address for an owner request, preferring the structured form over the flat
     * one. When {@code addressLine1} is supplied it is used (normalized), with the normalized
     * {@code addressLine2} appended after a single space when present; otherwise the flat
     * {@code address} is used (normalized). This is what every step that reads an owner's address —
     * validation, storage and household identity — sees, so structured and flat requests behave
     * identically downstream. Yields the empty string when no address is supplied in either form.
     */
    static String compose(String addressLine1, String addressLine2, String address) {
        if (!StringUtils.hasText(addressLine1)) {
            return normalize(address);
        }
        String line1 = normalize(addressLine1);
        return StringUtils.hasText(addressLine2) ? line1 + ' ' + normalize(addressLine2) : line1;
    }
}
