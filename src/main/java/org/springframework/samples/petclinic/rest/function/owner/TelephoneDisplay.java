package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Formats a stored E.164 telephone for humans: the '+' and country code, a space, then the
 * national digits grouped in threes — so '+61412345678' becomes '+61 412 345 678'. The raw
 * stored {@code telephone} stays E.164; this only produces the {@code telephoneDisplay} form.
 *
 * <p>Country-code detection reuses {@link TelephoneNormalizer#splitCountryCode(String)}, so the
 * split stays consistent with normalization. A value that is not canonical E.164 (e.g. legacy
 * data) is returned unchanged rather than mangled.
 */
public final class TelephoneDisplay {

    private TelephoneDisplay() {
    }

    /**
     * Human-readable form of a stored telephone, or the value unchanged when it is not canonical
     * E.164 ('+' followed by 8 to 15 digits); {@code null} in yields {@code null} out.
     */
    public static String of(String telephone) {
        if (telephone == null) {
            return null;
        }
        String trimmed = telephone.trim();
        if (!trimmed.matches("\\+[0-9]{8,15}")) {
            return telephone;
        }
        String[] parts = TelephoneNormalizer.splitCountryCode(trimmed.substring(1));
        return "+" + parts[0] + " " + groupInThrees(parts[1]);
    }

    /** Group the national digits left-to-right in blocks of three, space-separated. */
    private static String groupInThrees(String national) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                sb.append(' ');
            }
            sb.append(national.charAt(i));
        }
        return sb.toString();
    }
}
