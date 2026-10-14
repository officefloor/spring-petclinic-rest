package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Normalizes owner addresses into a canonical form, used both for storage and for comparing addresses.
 */
public final class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private AddressNormalizer() {
    }

    /**
     * Trims the address, collapses runs of whitespace into a single space, upper-cases it and expands
     * the whole-word abbreviations ST, RD and AVE (optionally followed by a '.') to STREET, ROAD and AVENUE.
     *
     * @param address the address as supplied
     * @return the normalized address, empty if the address is {@code null} or blank
     */
    public static String normalize(String address) {
        if (address == null || address.isBlank()) {
            return "";
        }
        return WHITESPACE.splitAsStream(address.strip().toUpperCase(Locale.ROOT))
            .map(AddressNormalizer::expand)
            .collect(Collectors.joining(" "));
    }

    private static String expand(String word) {
        String bare = word.endsWith(".") ? word.substring(0, word.length() - 1) : word;
        return ABBREVIATIONS.getOrDefault(bare, word);
    }
}
