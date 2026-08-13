package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Shared normalization of an owner's {@code address}, applied whenever an owner is
 * created. The canonical form is: trimmed, runs of whitespace collapsed to a single
 * space, upper-cased, and common street-type abbreviations expanded as whole words
 * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}).
 *
 * <p>So {@code "  12  main  st "} becomes {@code "12 MAIN STREET"} and {@code "7 elm ave"}
 * becomes {@code "7 ELM AVENUE"}. The stored and returned address is this form, the
 * required-field check rejects an address that is blank after it, and every address
 * comparison (household duplicate detection and the shared household id) uses it.
 */
public final class AddressNormalizer {

    /** Whole-word street-type abbreviations expanded to their canonical form. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /**
     * Returns the canonical form of {@code raw}, or {@code null} when {@code raw} is
     * {@code null}. A value that is blank (or whitespace only) normalizes to the empty
     * string, which the required-field check treats as absent.
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String collapsed = raw.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
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
     * Composes the address string returned to the client from already-normalized address
     * lines: {@code line1}, with a single space and {@code line2} appended when
     * {@code line2} is present (non-null and non-blank). Returns {@code line1} unchanged
     * when {@code line2} is absent.
     */
    public static String compose(String line1, String line2) {
        if (line2 == null || line2.isBlank()) {
            return line1;
        }
        return line1 + " " + line2;
    }
}
