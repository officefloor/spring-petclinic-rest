package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Canonicalises a free-text street address so it is stored, returned and compared in a
 * single form. Normalization: trim and collapse runs of whitespace to a single space,
 * upper-case, and expand common street-type abbreviations token-by-token
 * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}).
 *
 * <p>The transform is idempotent, so applying it to an already-normalized address (e.g.
 * one read back from an existing owner) yields the same value.
 */
final class AddressNormalizer {

    /** Upper-cased abbreviation -> its expansion. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /**
     * @return the normalized address, or {@code ""} when {@code value} is {@code null} or
     *         blank after trimming.
     */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        String[] tokens = trimmed.toUpperCase(Locale.ROOT).split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(token, token));
        }
        return sb.toString();
    }
}
