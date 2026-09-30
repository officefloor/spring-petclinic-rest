package org.springframework.samples.petclinic.rest.validation;

/**
 * Normalizes free-form telephone input into canonical E.164 form.
 *
 * <p>Rules:
 * <ul>
 *   <li>Spaces, dashes, brackets and any other non-digit separators are stripped.</li>
 *   <li>A leading {@code '+'} means the country code is already present and is kept.</li>
 *   <li>Otherwise the Australian country code {@code +61} is assumed and a single
 *       leading {@code '0'} is dropped from the national digits.</li>
 *   <li>The result must carry between 8 and 15 digits after the {@code '+'}, otherwise
 *       it cannot form a valid E.164 number.</li>
 *   <li>For a recognised country code the national number (the digits after the country
 *       code) must have the exact length that country requires: {@code +61} needs 9
 *       national digits and {@code +1} needs 10. A wrong national length for the country
 *       is rejected.</li>
 * </ul>
 *
 * <p>For example {@code '0412 345 678'} becomes {@code '+61412345678'} and
 * {@code '+64 21 123 456'} becomes {@code '+6421123456'}.
 */
public final class TelephoneNormalizer {

    private TelephoneNormalizer() {
    }

    /**
     * Converts raw telephone input to E.164, or returns {@code null} when the input is
     * {@code null} or cannot form a valid E.164 number.
     *
     * @param raw the raw telephone input (may be {@code null})
     * @return the E.164 string (e.g. {@code '+61412345678'}), or {@code null}
     */
    public static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        boolean hasCountryCode = raw.trim().startsWith("+");
        String digits = raw.replaceAll("\\D", "");
        if (!hasCountryCode) {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            digits = "61" + digits;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            return null;
        }
        if (!hasValidNationalLength(digits)) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Checks the national-number length against the country code carried by {@code digits}
     * (the E.164 digits without the leading {@code '+'}). {@code +61} requires 9 national
     * digits and {@code +1} requires 10; the national number is whatever follows the
     * country code. Country codes without a known length pass this check and are governed
     * only by the generic 8-to-15 total-digit rule.
     *
     * @param digits the E.164 digits without the leading {@code '+'}
     * @return {@code true} when the national-number length is acceptable for the country
     */
    private static boolean hasValidNationalLength(String digits) {
        if (digits.startsWith("61")) {
            return digits.length() - 2 == 9;
        }
        if (digits.startsWith("1")) {
            return digits.length() - 1 == 10;
        }
        return true;
    }

    /**
     * @param raw the raw telephone input (may be {@code null})
     * @return {@code true} when {@code raw} is {@code null} (absence is handled elsewhere)
     *         or can form a valid E.164 number
     */
    public static boolean isValid(String raw) {
        return raw == null || toE164(raw) != null;
    }
}
