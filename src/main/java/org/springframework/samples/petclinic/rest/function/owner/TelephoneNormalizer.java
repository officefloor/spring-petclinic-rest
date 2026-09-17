package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Canonical telephone normalization shared by the create pipeline: {@link NormalizeTelephone} stores
 * the E.164 form and {@link EnsureUniqueTelephone} compares by it. Not a pipeline step, so it is free
 * to expose plain helpers.
 */
public final class TelephoneNormalizer {

    /** Minimum digits after the '+' for a valid E.164 number. */
    private static final int MIN_E164_DIGITS = 8;

    /** Maximum digits after the '+' for a valid E.164 number. */
    private static final int MAX_E164_DIGITS = 15;

    /** Default country code assumed when no leading '+' is present. */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    private TelephoneNormalizer() {
    }

    /**
     * Converts a raw telephone to canonical E.164 form, or returns {@code null} when it cannot form a
     * valid E.164 number. A leading '+' and country code are kept when present; otherwise the default
     * country code '+61' is assumed and a single leading '0' is dropped from the national digits.
     * Spaces, dashes and brackets (any non-digit) are stripped. The result must have 8 to 15 digits
     * after the '+'.
     */
    public static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        boolean hasCountryCode = telephone.trim().startsWith("+");
        String digits = telephone.replaceAll("\\D", "");
        if (!hasCountryCode) {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            digits = DEFAULT_COUNTRY_CODE + digits;
        }
        if (digits.length() < MIN_E164_DIGITS || digits.length() > MAX_E164_DIGITS) {
            return null;
        }
        return "+" + digits;
    }
}
