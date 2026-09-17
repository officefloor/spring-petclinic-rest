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

    /**
     * Composes the flat address from a structured address: the {@link #normalize(String) normalized}
     * first line, with a single space and the normalized second line appended when a second line is
     * present. A blank second line yields just the first line.
     */
    public static String compose(String addressLine1, String addressLine2) {
        String line1 = normalize(addressLine1);
        String line2 = normalize(addressLine2);
        if (line2.isEmpty()) {
            return line1;
        }
        return line1.isEmpty() ? line2 : line1 + " " + line2;
    }

    /**
     * Resolves an owner's canonical address from a request that may use the structured form
     * ({@code addressLine1}/{@code addressLine2}) or the flat {@code address}. The structured form is
     * preferred whenever {@code addressLine1} is non-blank: each line is normalized and the flat
     * address is their {@link #compose(String, String) composition}. Otherwise only the flat address
     * is normalized and no structured lines are kept, keeping the contract backward-compatible.
     */
    public static Normalized resolve(String addressLine1, String addressLine2, String flatAddress) {
        String line1 = normalize(addressLine1);
        if (line1.isEmpty()) {
            return new Normalized(null, null, normalize(flatAddress));
        }
        String line2 = normalize(addressLine2);
        return new Normalized(line1, line2.isEmpty() ? null : line2, compose(addressLine1, addressLine2));
    }

    /**
     * A resolved address: the normalized structured lines ({@code null} when the flat form was used
     * or a line was absent) and the flat {@code address} to store and return.
     */
    public record Normalized(String addressLine1, String addressLine2, String address) {
    }
}
