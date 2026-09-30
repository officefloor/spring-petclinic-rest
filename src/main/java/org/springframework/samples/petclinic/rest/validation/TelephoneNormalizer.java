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
        return "+" + digits;
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
