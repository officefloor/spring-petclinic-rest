package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Canonical form of an owner's postal {@code address}, applied whenever an owner is created. Trims
 * leading/trailing whitespace, collapses runs of whitespace to a single space, upper-cases, and expands
 * common street-type abbreviations as whole words ({@code ST -> STREET}, {@code RD -> ROAD},
 * {@code AVE -> AVENUE}). So {@code '  12  main  st '} becomes {@code '12 MAIN STREET'} and
 * {@code '7 elm ave'} becomes {@code '7 ELM AVENUE'}. A {@code null} or whitespace-only address
 * normalizes to the empty string.
 *
 * <p>This is the single normalized form used everywhere an address matters: it is what
 * {@link NormalizeAddress} stores on the request (so it is persisted and returned), what
 * {@link RequireOwnerFields} tests for blankness, and what {@link HouseholdId} compares, so both the
 * derived {@link IdentityKey} used for duplicate detection and the shared household id agree.
 */
public final class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
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
