package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * The single normalization rule for owner addresses: trim, collapse runs of whitespace to a
 * single space, upper-case, and expand common street-type abbreviations (ST->STREET, RD->ROAD,
 * AVE->AVENUE). Also composes the flat address from the structured {@code addressLine1}/
 * {@code addressLine2} fields. Shared by {@link NormalizeOwnerAddress} (which stores the
 * normalized value on the request) so every address is stored, returned and compared —
 * duplicate-household detection and the shared household id (see {@link OwnerHouseholds}) — in
 * the same form.
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

    /**
     * The flat address composed from two already-normalized structured lines: {@code line1} on
     * its own, or {@code line1} then a single space then {@code line2} when {@code line2} is
     * present (non-empty). A blank second line is treated as absent.
     */
    static String compose(String line1, String line2) {
        return (line2 == null || line2.isEmpty()) ? line1 : line1 + " " + line2;
    }

    /** Whether an address line was supplied, i.e. is non-null and not blank. */
    static boolean isPresent(String line) {
        return line != null && !line.isBlank();
    }
}
