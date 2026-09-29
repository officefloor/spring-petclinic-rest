package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * The single definition of the normalized form of an owner address. Shared by
 * {@link NormalizeOwnerAddress} (which stores the normalized value) and {@link HouseholdKey}
 * (which compares against it for duplicate detection and the shared household id), so every place
 * that stores, returns or compares an address applies the same rule and they cannot drift apart.
 *
 * <p>The rule: trim, collapse runs of whitespace to a single space, upper-case, and expand the
 * common street-type abbreviations word-by-word ({@code ST -> STREET}, {@code RD -> ROAD},
 * {@code AVE -> AVENUE}). A {@code null} or whitespace-only value normalizes to the empty string.
 *
 * <p>Kept out of the function classes themselves because OfficeFloor requires each function class to
 * expose exactly one public method.
 */
final class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    /** Returns the normalized form of the given address; {@code null}/blank yields {@code ""}. */
    static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase();
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] words = collapsed.split(" ");
        for (int i = 0; i < words.length; i++) {
            String expanded = ABBREVIATIONS.get(words[i]);
            if (expanded != null) {
                words[i] = expanded;
            }
        }
        return String.join(" ", words);
    }

    private AddressNormalizer() {
    }
}
