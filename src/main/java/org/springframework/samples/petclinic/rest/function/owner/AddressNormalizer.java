package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * The single home of an owner's address logic: the normalized form of an address line, the
 * "structured form supplied?" predicate, and the composition of the two structured lines into a
 * single address. Every place that stores, validates or returns an address goes through here, so the
 * rules cannot drift apart. (The household id is keyed on {@code (lastName, postcode)}, not the
 * address — see {@link HouseholdKey}.)
 *
 * <p>The normalization rule: trim, collapse runs of whitespace to a single space, upper-case, and
 * expand the common street-type abbreviations word-by-word ({@code ST -> STREET}, {@code RD -> ROAD},
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

    /**
     * Whether the request supplied the structured address form — i.e. {@code addressLine1} is present
     * (non-blank). The single answer to "prefer the structured fields?", shared by every step that
     * decides between the structured and the flat {@code address} form.
     */
    static boolean hasStructuredAddress(String addressLine1) {
        return addressLine1 != null && !addressLine1.isBlank();
    }

    /**
     * Composes the two structured lines into one address: {@code line1}, with a single space and
     * {@code line2} appended when {@code line2} is present (non-blank). This owns only the join shape;
     * normalization is the caller's concern — the create path composes already-normalized lines, the
     * update path composes the raw values (mirroring that the update pipeline does not normalize).
     */
    static String compose(String line1, String line2) {
        String base = line1 == null ? "" : line1;
        if (line2 == null || line2.isBlank()) {
            return base;
        }
        return base + " " + line2;
    }

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
