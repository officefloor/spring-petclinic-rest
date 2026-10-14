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

    /**
     * Normalizes an optional address field.
     *
     * @param address the address field as supplied
     * @return the {@link #normalize(String) normalized} field, or {@code null} if it is {@code null} or blank
     */
    public static String normalizeOptional(String address) {
        String normalized = normalize(address);
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * The owner's effective address. When the structured {@code addressLine1} is present it is preferred: the
     * normalized line 1, followed by a single space and the normalized line 2 when that is present. Otherwise
     * the normalized flat address.
     *
     * @param addressLine1 the structured first address line, may be {@code null}
     * @param addressLine2 the structured second address line, may be {@code null}
     * @param address the flat address, may be {@code null}
     * @return the normalized effective address, empty if no address is supplied
     */
    public static String compose(String addressLine1, String addressLine2, String address) {
        String line1 = normalizeOptional(addressLine1);
        if (line1 == null) {
            return normalize(address);
        }
        String line2 = normalizeOptional(addressLine2);
        return line2 == null ? line1 : line1 + " " + line2;
    }

    private static String expand(String word) {
        String bare = word.endsWith(".") ? word.substring(0, word.length() - 1) : word;
        return ABBREVIATIONS.getOrDefault(bare, word);
    }
}
