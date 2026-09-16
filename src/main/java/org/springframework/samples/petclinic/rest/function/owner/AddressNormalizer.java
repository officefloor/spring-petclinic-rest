package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Shared address normalization for the owner pipelines: trim, collapse internal
 * whitespace, upper-case, and expand common street-type abbreviations
 * ({@code ST->STREET}, {@code RD->ROAD}, {@code AVE->AVENUE}, matched whole-word).
 *
 * <p>Used by {@link ValidateOwnerFields} so a created owner is stored and returned
 * with the canonical address (and the required-field check rejects an address that is
 * blank once normalized). The transform is idempotent, so re-normalizing an
 * already-normalized address is a no-op.
 */
final class AddressNormalizer {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private AddressNormalizer() {
    }

    /**
     * @return the canonical form of {@code address}, or {@code ""} when it is null or
     *         blank (so callers can treat a blank-after-normalization address uniformly).
     */
    static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = WHITESPACE.matcher(address.strip()).replaceAll(" ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder normalized = new StringBuilder(collapsed.length());
        for (String token : tokens) {
            if (normalized.length() > 0) {
                normalized.append(' ');
            }
            normalized.append(expand(token));
        }
        return normalized.toString();
    }

    /** Expand a single street-type abbreviation to its full word, else leave it unchanged. */
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
