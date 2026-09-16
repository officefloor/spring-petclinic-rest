package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * The single normalization rule for owner addresses: trim, collapse runs of whitespace to a
 * single space, upper-case, and expand common street-type abbreviations (ST->STREET, RD->ROAD,
 * AVE->AVENUE). Shared by {@link NormalizeOwnerAddress} (which stores the normalized value on the
 * request) so every address is stored, returned and compared — duplicate-household detection and
 * the shared household id (see {@link OwnerHouseholds}) — in the same form.
 */
final class OwnerAddresses {

    /** Upper-cased abbreviation -> its expansion, matched a whole word at a time. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private OwnerAddresses() {
    }

    /**
     * The stored form of an address: trimmed, internal whitespace collapsed, upper-cased and with
     * common street-type abbreviations expanded. A {@code null} or blank input normalizes to the
     * empty string.
     */
    static String normalize(String address) {
        if (address == null || address.isBlank()) {
            return "";
        }
        String[] words = address.trim().toUpperCase(Locale.ROOT).split("\\s+");
        StringBuilder normalized = new StringBuilder(address.length());
        for (String word : words) {
            if (normalized.length() > 0) {
                normalized.append(' ');
            }
            normalized.append(ABBREVIATIONS.getOrDefault(word, word));
        }
        return normalized.toString();
    }
}
