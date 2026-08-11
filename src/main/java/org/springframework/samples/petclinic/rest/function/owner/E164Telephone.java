package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Normalizes a telephone number to E.164 form:
 * <ul>
 *   <li>separators (spaces, dashes, brackets and the like) are stripped;</li>
 *   <li>a leading {@code '+'} and country code are kept when present;</li>
 *   <li>otherwise country code {@code '+61'} is assumed and a single leading {@code '0'}
 *       is dropped from the national digits;</li>
 *   <li>the result must have 8 to 15 digits after the {@code '+'};</li>
 *   <li>the national-number length must match the country code: {@code '+61'} requires 9
 *       national digits and {@code '+1'} requires 10.</li>
 * </ul>
 * So {@code "0412 345 678"} becomes {@code "+61412345678"}. Returns {@code null} when the
 * input cannot form a valid E.164 number.
 *
 * <p>{@link #display(String)} does the inverse presentation step: it renders a stored E.164
 * number back for humans (country code, space, national digits grouped in threes), e.g.
 * {@code "+61412345678"} becomes {@code "+61 412 345 678"}.
 */
public final class E164Telephone {

    private static final int MIN_DIGITS = 8;
    private static final int MAX_DIGITS = 15;

    /** National-number length required for country code {@code '+61'}. */
    private static final int AU_NATIONAL_DIGITS = 9;

    /** National-number length required for country code {@code '+1'}. */
    private static final int NANP_NATIONAL_DIGITS = 10;

    private E164Telephone() {
    }

    static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        boolean explicitCountryCode = trimmed.startsWith("+");
        String digits = trimmed.replaceAll("\\D", "");
        if (!explicitCountryCode) {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            digits = "61" + digits;
        }
        if (digits.length() < MIN_DIGITS || digits.length() > MAX_DIGITS) {
            return null;
        }
        // National-number length must match the country code.
        if (digits.startsWith("61")) {
            if (digits.length() - 2 != AU_NATIONAL_DIGITS) {
                return null;
            }
        }
        else if (digits.startsWith("1")) {
            if (digits.length() - 1 != NANP_NATIONAL_DIGITS) {
                return null;
            }
        }
        return "+" + digits;
    }

    /**
     * Formats a stored E.164 number for humans: a {@code '+'} and country code, a space, then the
     * national digits grouped in threes (e.g. {@code "+61412345678"} becomes
     * {@code "+61 412 345 678"}). The country code is taken as one digit for the {@code '+1'} NANP
     * and two digits otherwise, matching the country codes this app normalizes to. Returns
     * {@code null} when the value is absent or not in E.164 (leading {@code '+'}) form.
     */
    public static String display(String e164) {
        if (e164 == null) {
            return null;
        }
        String trimmed = e164.trim();
        if (!trimmed.startsWith("+")) {
            return null;
        }
        String digits = trimmed.substring(1).replaceAll("\\D", "");
        int countryCodeLength = digits.startsWith("1") ? 1 : 2;
        if (digits.length() <= countryCodeLength) {
            return null;
        }
        String national = digits.substring(countryCodeLength);
        StringBuilder display = new StringBuilder("+").append(digits, 0, countryCodeLength);
        for (int i = 0; i < national.length(); i += 3) {
            display.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return display.toString();
    }
}
