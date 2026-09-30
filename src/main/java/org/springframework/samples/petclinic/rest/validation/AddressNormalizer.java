package org.springframework.samples.petclinic.rest.validation;

/**
 * Normalizes free-form postal address input into a canonical stored form.
 *
 * <p>Rules:
 * <ul>
 *   <li>Leading and trailing whitespace is trimmed and internal runs of whitespace
 *       are collapsed to a single space.</li>
 *   <li>The text is upper-cased.</li>
 *   <li>Common street-type abbreviations are expanded token-by-token:
 *       {@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}.</li>
 * </ul>
 *
 * <p>For example {@code '  12  main  st '} becomes {@code '12 MAIN STREET'} and
 * {@code '7 elm ave'} becomes {@code '7 ELM AVENUE'}.
 */
public final class AddressNormalizer {

    private AddressNormalizer() {
    }

    /**
     * Normalizes raw address input, or returns {@code null} when the input is {@code null}.
     * An input that is blank (empty once whitespace is collapsed) normalizes to an empty
     * string, so the required-field check rejects it.
     *
     * @param raw the raw address input (may be {@code null})
     * @return the normalized address (e.g. {@code '12 MAIN STREET'}), or {@code null}
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String collapsed = raw.trim().replaceAll("\\s+", " ").toUpperCase();
        if (collapsed.isEmpty()) {
            return collapsed;
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
        return switch (token) {
            case "ST" -> "STREET";
            case "RD" -> "ROAD";
            case "AVE" -> "AVENUE";
            default -> token;
        };
    }
}
