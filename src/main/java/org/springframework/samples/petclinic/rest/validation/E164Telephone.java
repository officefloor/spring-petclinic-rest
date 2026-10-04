package org.springframework.samples.petclinic.rest.validation;

/**
 * Converts a raw, user-supplied telephone number into E.164 form.
 *
 * <p>The rules are:
 * <ul>
 *     <li>spaces, dashes and brackets are stripped;</li>
 *     <li>a leading {@code '+'} and country code are kept when present;</li>
 *     <li>otherwise country code {@code '+61'} is assumed and a single leading
 *         {@code '0'} is dropped from the national digits;</li>
 *     <li>the result must carry 8 to 15 digits after the {@code '+'}.</li>
 * </ul>
 *
 * <p>So {@code "0412 345 678"} becomes {@code "+61412345678"} and
 * {@code "+64 21 123 456"} becomes {@code "+6421123456"}. The normalization is
 * idempotent: feeding it an already-E.164 value returns that same value.
 */
public final class E164Telephone {

    private static final String DEFAULT_COUNTRY_CODE = "61";

    private E164Telephone() {
    }

    /**
     * Normalize a raw telephone number to E.164 form, or return {@code null} when the
     * input is {@code null} or cannot form a valid E.164 number.
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        // strip spaces, dashes and brackets
        String cleaned = raw.replaceAll("[\\s\\-()]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            if (cleaned.startsWith("0")) {
                cleaned = cleaned.substring(1);
            }
            digits = DEFAULT_COUNTRY_CODE + cleaned;
        }
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Whether the raw value can be converted to a valid E.164 number.
     */
    public static boolean isValid(String raw) {
        return normalize(raw) != null;
    }
}
