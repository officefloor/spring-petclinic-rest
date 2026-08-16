package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Normalizes a street address to a canonical stored form.
 *
 * <p>Rules: trim the ends, collapse every run of whitespace to a single space, upper-case the
 * whole string, then expand common abbreviations as whole words: {@code ST -> STREET},
 * {@code RD -> ROAD}, {@code AVE -> AVENUE}. Expansion is token-based, so an abbreviation embedded
 * in a longer word (e.g. {@code STANMORE}) is left untouched. A {@code null} or all-whitespace
 * input yields the empty string, which the required-field check treats as a blank address.
 *
 * <p>Example: {@code "  12  main  st "} becomes {@code "12 MAIN STREET"}.
 */
final class AddressNormalizer {

    /** Any run of whitespace, collapsed to a single space. */
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private AddressNormalizer() {
    }

    /** Convert to the canonical stored form; {@code null}/blank input yields {@code ""}. */
    static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String collapsed = WHITESPACE.matcher(raw.trim()).replaceAll(" ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(expand(tokens[i]));
        }
        return sb.toString();
    }

    /**
     * Compose the canonical stored address from the structured lines: the normalized
     * {@code addressLine1}, with a single space and the normalized {@code addressLine2} appended when
     * that second line is present (non-blank after normalization). Both inputs are normalized here,
     * so callers may pass the raw request values.
     */
    static String compose(String addressLine1, String addressLine2) {
        String line1 = normalize(addressLine1);
        String line2 = normalize(addressLine2);
        return line2.isEmpty() ? line1 : line1 + " " + line2;
    }

    /** Expand a single whole-word abbreviation, or return the token unchanged. */
    private static String expand(String token) {
        switch (token) {
            case "ST":
                return "STREET";
            case "RD":
                return "ROAD";
            case "AVE":
                return "AVENUE";
            default:
                return token;
        }
    }
}
