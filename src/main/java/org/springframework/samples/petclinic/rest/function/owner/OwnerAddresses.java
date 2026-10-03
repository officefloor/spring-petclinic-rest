package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * The single-sourced definition of a normalized postal address, shared by the
 * create-time {@link NormalizeOwnerAddress} step (which stores the normalized value)
 * and {@link HouseholdKey} (so every address comparison uses the same normalized form).
 *
 * <p>Normalization trims the value, collapses any run of whitespace to a single space,
 * upper-cases it and expands common street-type abbreviations ({@code ST} &rarr;
 * {@code STREET}, {@code RD} &rarr; {@code ROAD}, {@code AVE} &rarr; {@code AVENUE}).
 */
final class OwnerAddresses {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private OwnerAddresses() {
    }

    /**
     * Normalize an address. Returns {@code ""} when {@code address} is {@code null} or
     * is blank once whitespace has been trimmed and collapsed.
     */
    static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.toUpperCase(Locale.ROOT).split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
    }
}
