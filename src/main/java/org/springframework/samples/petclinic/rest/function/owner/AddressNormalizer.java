package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Canonical address normalization shared by the create pipeline: {@link NormalizeAddress} stores and
 * returns the normalized form and {@link HouseholdNormalizer} compares addresses by it. Not a pipeline
 * step, so it is free to expose plain helpers.
 */
public final class AddressNormalizer {

    /** Common street-type abbreviations expanded to their full word (keys are already upper-cased). */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /**
     * Normalizes an address to its canonical stored/compared form: outer whitespace trimmed, internal
     * runs of whitespace collapsed to a single space, upper-cased, and common street-type
     * abbreviations expanded as whole words ({@code ST}&rarr;{@code STREET}, {@code RD}&rarr;{@code
     * ROAD}, {@code AVE}&rarr;{@code AVENUE}). A {@code null} or blank address normalizes to an empty
     * string. Idempotent: normalizing an already-normalized address returns it unchanged.
     */
    public static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase();
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder result = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                result.append(' ');
            }
            result.append(ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return result.toString();
    }
}
