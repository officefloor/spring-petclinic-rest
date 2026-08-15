package org.springframework.samples.petclinic.util;

/**
 * Formats an owner's stored E.164 {@code telephone} for humans: the country code, a space, then
 * the national digits grouped in threes. So {@code "+61412345678"} becomes {@code "+61 412 345
 * 678"}. The country code is recognised from a fixed set of known prefixes (matched
 * longest-prefix-first, mirroring the normalization on the way in); anything after it is the
 * national number. If the number is not in E.164 form or carries an unrecognised country code the
 * digits are simply grouped in threes without a separate country-code segment.
 */
public final class TelephoneDisplay {

    /** Known country codes (without '+'), longest-prefix-first so '61' is tried before '1'. */
    private static final String[] COUNTRY_CODES = {"61", "1"};

    private TelephoneDisplay() {
    }

    /**
     * Returns the human-formatted telephone for the given E.164 string, or {@code null} when
     * {@code telephone} is {@code null}.
     */
    public static String format(String telephone) {
        if (telephone == null) {
            return null;
        }
        String digits = telephone.startsWith("+") ? telephone.substring(1) : telephone;
        for (String code : COUNTRY_CODES) {
            if (digits.startsWith(code)) {
                return "+" + code + " " + groupInThrees(digits.substring(code.length()));
            }
        }
        return "+" + groupInThrees(digits);
    }

    /** Groups the digits left-to-right in blocks of three, separated by single spaces. */
    private static String groupInThrees(String digits) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                sb.append(' ');
            }
            sb.append(digits.charAt(i));
        }
        return sb.toString();
    }
}
