package org.springframework.samples.petclinic.rest.validation;

/**
 * Normalizes a telephone number to canonical E.164 form.
 *
 * <p>Formatting characters (spaces, dashes and brackets) are stripped. A number written with a
 * leading {@code '+'} keeps its explicit country code; otherwise the Australian country code
 * {@code +61} is assumed and a single leading {@code '0'} is dropped from the national digits.
 * The result must carry between 8 and 15 digits after the {@code '+'} to be valid.
 */
public final class TelephoneNormalizer {

    /** Minimum number of digits allowed after the leading {@code '+'} in E.164. */
    private static final int MIN_DIGITS = 8;

    /** Maximum number of digits allowed after the leading {@code '+'} in E.164. */
    private static final int MAX_DIGITS = 15;

    /** Country code assumed when the number carries no explicit {@code '+'} prefix. */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    private TelephoneNormalizer() {
    }

    /**
     * Convert a raw telephone to E.164 form.
     *
     * @param telephone the raw telephone, possibly {@code null} or containing formatting characters
     * @return the E.164 string (a {@code '+'} followed by 8 to 15 digits), or {@code null} when the
     *         input cannot form a valid E.164 number
     */
    public static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[\\s\\-()]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = DEFAULT_COUNTRY_CODE + national;
        }
        if (!digits.matches("\\d{" + MIN_DIGITS + "," + MAX_DIGITS + "}")) {
            return null;
        }
        return "+" + digits;
    }
}
