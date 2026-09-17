package org.springframework.samples.petclinic.rest.validation;

/**
 * Formats a canonical E.164 telephone number for human reading.
 *
 * <p>The country calling code is separated from the national number, which is then grouped into
 * blocks of three digits, e.g. {@code "+61412345678"} becomes {@code "+61 412 345 678"}. When the
 * number carries no recognizable country code (see {@link E164NationalNumberRule#countryCode(String)})
 * it is returned unchanged, since its country/national split is unknown.
 */
public final class TelephoneFormatter {

    /** Number of national digits per display group. */
    private static final int GROUP_SIZE = 3;

    private TelephoneFormatter() {
    }

    /**
     * Format an E.164 telephone for display.
     *
     * @param e164 a normalized E.164 string ({@code '+'} followed by digits), as produced by
     *             {@link TelephoneNormalizer#toE164(String)}; may be {@code null}
     * @return the human-readable form ({@code "+<countryCode> <grouped national digits>"}), or the
     *         input unchanged when it is {@code null} or has no known country code
     */
    public static String toDisplay(String e164) {
        String countryCode = E164NationalNumberRule.countryCode(e164);
        if (countryCode == null) {
            return e164;
        }
        String national = e164.substring(1 + countryCode.length());
        return "+" + countryCode + " " + groupInThrees(national);
    }

    /** Groups {@code digits} into space-separated blocks of {@link #GROUP_SIZE}, counting from the left. */
    private static String groupInThrees(String digits) {
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % GROUP_SIZE == 0) {
                grouped.append(' ');
            }
            grouped.append(digits.charAt(i));
        }
        return grouped.toString();
    }
}
