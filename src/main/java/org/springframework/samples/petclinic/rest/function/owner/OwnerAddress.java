package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Shared normalization for the owner postal address. The canonical stored and returned form is
 * trimmed, has internal whitespace collapsed to single spaces, is upper-cased and has common
 * street-type abbreviations expanded ({@code ST -> STREET}, {@code RD -> ROAD},
 * {@code AVE -> AVENUE}). Applied when an owner is created so the stored address, the required-field
 * check and every household comparison (duplicate detection and the shared household id) all use the
 * same form.
 */
final class OwnerAddress {

    /** Whole-token street-type abbreviations expanded to their full form. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private OwnerAddress() {
    }

    /**
     * @return the normalized address (trimmed, whitespace-collapsed, upper-cased, abbreviations
     * expanded), or an empty string when {@code address} is {@code null} or blank after trimming.
     */
    static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
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
