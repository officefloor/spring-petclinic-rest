package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.Map;

/**
 * Normalizes a postal address to a single canonical form: leading and trailing whitespace
 * is trimmed, internal runs of whitespace collapse to one space, the text is upper-cased,
 * and common street-type abbreviations are expanded to their full word.
 *
 * <p>So {@code "  12  main  st "} becomes {@code "12 MAIN STREET"} and {@code "7 elm ave"}
 * becomes {@code "7 ELM AVENUE"}. An input that is {@code null} or blank normalizes to the
 * empty string.
 */
public final class AddressNormalizer {

    /** Street-type abbreviations expanded to their full word (compared per whole token). */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /**
     * @param raw the caller-supplied address, in any casing and spacing
     * @return the canonical address, or the empty string when {@code raw} is null or blank
     */
    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String[] tokens = raw.trim().toUpperCase(Locale.ROOT).split("\\s+");
        StringBuilder sb = new StringBuilder(raw.length());
        for (String token : tokens) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(token, token));
        }
        return sb.toString();
    }

    /**
     * Composes a structured address into a single canonical string: the normalized
     * {@code line1}, with a single space and the normalized {@code line2} appended when
     * {@code line2} is present (non-blank). Each line is normalized via {@link #normalize}.
     *
     * @param line1 the first address line (any casing and spacing)
     * @param line2 the optional second address line; ignored when null or blank
     * @return the composed, normalized address
     */
    public static String compose(String line1, String line2) {
        String first = normalize(line1);
        String second = normalize(line2);
        return second.isEmpty() ? first : first + " " + second;
    }
}
