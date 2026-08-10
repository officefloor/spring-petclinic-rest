package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Normalizes a street address into a single canonical form used both for storage and for every
 * address comparison (household duplicate detection and the shared household id):
 * <ul>
 *   <li>leading and trailing whitespace is trimmed and internal whitespace runs are collapsed to a
 *       single space;</li>
 *   <li>the text is upper-cased;</li>
 *   <li>common trailing-type abbreviations are expanded as whole words: {@code ST -> STREET},
 *       {@code RD -> ROAD}, {@code AVE -> AVENUE}.</li>
 * </ul>
 * So {@code "  12  main  st "} becomes {@code "12 MAIN STREET"}. Returns {@code ""} for a
 * {@code null} or whitespace-only input, so callers can treat a blank-after-normalization address
 * as absent.
 */
final class AddressNormalizer {

    private AddressNormalizer() {
    }

    static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String collapsed = raw.trim().replaceAll("\\s+", " ").toUpperCase();
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
