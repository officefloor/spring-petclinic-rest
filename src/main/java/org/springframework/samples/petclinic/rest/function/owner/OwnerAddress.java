package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Shared address handling for owner endpoints. An owner's address is stored, returned and compared
 * in a canonical form: trimmed with internal whitespace runs collapsed to a single space,
 * upper-cased, and with the common street-type abbreviations expanded — {@code ST -> STREET},
 * {@code RD -> ROAD}, {@code AVE -> AVENUE} (whole tokens only). So {@code '  12  main  st '}
 * becomes {@code '12 MAIN STREET'}.
 */
final class OwnerAddress {

    private OwnerAddress() {
    }

    /**
     * Returns the normalized address, or {@code null} when the input is null or blank after
     * trimming and collapsing whitespace — so a required-field check can reject it.
     */
    static String normalize(String address) {
        if (address == null) {
            return null;
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return null;
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
     * Normalized address for comparison, never {@code null}: a null or blank address collapses to
     * the empty string so two addressless owners compare equal.
     */
    static String forComparison(String address) {
        String normalized = normalize(address);
        return normalized == null ? "" : normalized;
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
