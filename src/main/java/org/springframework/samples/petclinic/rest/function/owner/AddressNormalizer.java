package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Canonicalises a raw postal address entry into its normalized form.
 *
 * <p>Rules: trim leading/trailing whitespace, collapse internal runs of whitespace to a single
 * space, upper-case, and expand common street-type abbreviations token-by-token
 * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). An address that is empty or
 * whitespace-only normalizes to the empty string.
 *
 * <p>The conversion is idempotent — feeding an already-normalized value back in returns it
 * unchanged — so it can normalize both incoming requests and already-stored values for
 * comparison (household duplicate detection and the shared household id).
 */
final class AddressNormalizer {

    private AddressNormalizer() {
    }

    /**
     * @param raw the raw address text (may be {@code null})
     * @return the normalized address, or {@code ""} when {@code raw} is blank after normalization
     */
    static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String collapsed = raw.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder result = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                result.append(' ');
            }
            result.append(expand(tokens[i]));
        }
        return result.toString();
    }

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
