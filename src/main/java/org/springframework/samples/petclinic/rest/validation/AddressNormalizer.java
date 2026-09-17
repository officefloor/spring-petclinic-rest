package org.springframework.samples.petclinic.rest.validation;

import java.util.Locale;
import java.util.Map;

/**
 * Normalizes a postal address to its canonical stored form.
 *
 * <p>The raw value is trimmed, its internal runs of whitespace are collapsed to a single space
 * and it is upper-cased. Each resulting word is then checked against a small table of common
 * abbreviations ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}) and expanded to
 * its full form. Applying this consistently means addresses that differ only in casing, spacing or
 * abbreviation are stored — and therefore compared — as the same value.
 */
public final class AddressNormalizer {

    /** Whole-word abbreviations expanded to their full form (keyed by their upper-cased token). */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /**
     * Convert a raw address to its normalized form.
     *
     * @param address the raw address, possibly {@code null} or containing irregular whitespace,
     *                casing or abbreviations
     * @return the normalized address (trimmed, whitespace-collapsed, upper-cased and with common
     *         abbreviations expanded), or {@code null} when the input is {@code null}
     */
    public static String normalize(String address) {
        if (address == null) {
            return null;
        }
        String collapsed = address.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return collapsed;
        }
        String[] words = collapsed.split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = ABBREVIATIONS.getOrDefault(words[i], words[i]);
        }
        return String.join(" ", words);
    }
}
