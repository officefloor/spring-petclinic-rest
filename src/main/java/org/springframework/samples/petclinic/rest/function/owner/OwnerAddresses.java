package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Shared address canonicalization for the owner pipelines: reduces an address to a single
 * normalized form so the required-field check, household comparisons and storage all use
 * the same value.
 *
 * <p>Normalization trims surrounding whitespace, collapses each internal run of whitespace
 * to a single space, upper-cases, and expands common street-type abbreviations
 * ({@code ST->STREET}, {@code RD->ROAD}, {@code AVE->AVENUE}) as whole words. It is
 * idempotent: normalizing an already-normalized address returns it unchanged.
 */
final class OwnerAddresses {

    /** Whole-word abbreviation expansions, keyed by their upper-cased form. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private OwnerAddresses() {
    }

    /**
     * Canonical form of {@code address}: trimmed, internal whitespace collapsed to single
     * spaces, upper-cased, with each abbreviation token expanded. A {@code null} address, or
     * one that is empty once trimmed, normalizes to {@code ""} (which the required-field check
     * then rejects as blank).
     */
    static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        StringBuilder normalized = new StringBuilder(collapsed.length());
        for (String token : collapsed.split(" ")) {
            if (normalized.length() > 0) {
                normalized.append(' ');
            }
            normalized.append(ABBREVIATIONS.getOrDefault(token, token));
        }
        return normalized.toString();
    }
}
