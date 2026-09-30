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

    /**
     * Composes the stored flat address from its (already-normalized) parts. When a non-blank
     * {@code line1} is present the structured form wins: the result is {@code line1}, with a single
     * space and {@code line2} appended when {@code line2} is present. Otherwise it falls back to the
     * supplied {@code flat} address, keeping the request backward-compatible.
     *
     * <p>Inputs are expected to be already normalized (see {@link #normalize(String)}); this method
     * only chooses and joins, it does not re-normalize.
     *
     * @param line1 the normalized structured first address line (may be {@code null}/blank)
     * @param line2 the normalized structured second address line (may be {@code null}/blank)
     * @param flat  the normalized flat address to fall back to (may be {@code null})
     * @return the composed address
     */
    public static String compose(String line1, String line2, String flat) {
        if (line1 != null && !line1.isBlank()) {
            return (line2 != null && !line2.isBlank()) ? line1 + " " + line2 : line1;
        }
        return flat;
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
