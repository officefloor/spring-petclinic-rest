package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Single source of truth for the owner address normalisation rule used across the
 * {@code POST /owners} pipeline: an address is canonicalised by trimming, collapsing internal
 * whitespace to single spaces, upper-casing, and expanding common abbreviations as whole words
 * ({@code ST}→{@code STREET}, {@code RD}→{@code ROAD}, {@code AVE}→{@code AVENUE}). So
 * {@code "  12  main  st "} canonicalises to {@code "12 MAIN STREET"} and {@code "7 elm ave"} to
 * {@code "7 ELM AVENUE"}.
 *
 * <p>Centralised here (rather than inlined) so its one consumer has a single definition to agree
 * on: {@link ValidateOwnerFields} stores the canonical value and rejects a value that is blank
 * once normalised.
 */
final class OwnerAddress {

    /** Whole-word abbreviations expanded during normalisation. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private OwnerAddress() {
    }

    /**
     * The stored/returned/compared form: trimmed, internal whitespace collapsed to single
     * spaces, upper-cased, with common abbreviations expanded as whole words. Returns an empty
     * string for a null or whitespace-only input.
     */
    static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String trimmed = address.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        String[] tokens = trimmed.toUpperCase(Locale.ROOT).split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(token, token));
        }
        return sb.toString();
    }
}
