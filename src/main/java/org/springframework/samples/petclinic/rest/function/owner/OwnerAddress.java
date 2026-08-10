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
     * Resolves the effective, normalized address from the two accepted forms. The structured form is
     * preferred: when {@code addressLine1} is non-blank the result is its normalized value, with a
     * single space and the normalized {@code addressLine2} appended when {@code addressLine2} is
     * present. Otherwise it falls back to the normalized flat {@code address}. Returns {@code null}
     * when no address is supplied in either form, so a required-field check can reject it.
     */
    static String compose(String addressLine1, String addressLine2, String address) {
        String line1 = normalize(addressLine1);
        if (line1 != null) {
            String line2 = normalize(addressLine2);
            return line2 == null ? line1 : line1 + " " + line2;
        }
        return normalize(address);
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
