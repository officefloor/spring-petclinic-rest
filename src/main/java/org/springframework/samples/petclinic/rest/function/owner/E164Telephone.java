package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Normalizes a telephone number to E.164 form:
 * <ul>
 *   <li>separators (spaces, dashes, brackets and the like) are stripped;</li>
 *   <li>a leading {@code '+'} and country code are kept when present;</li>
 *   <li>otherwise country code {@code '+61'} is assumed and a single leading {@code '0'}
 *       is dropped from the national digits;</li>
 *   <li>the result must have 8 to 15 digits after the {@code '+'}.</li>
 * </ul>
 * So {@code "0412 345 678"} becomes {@code "+61412345678"}. Returns {@code null} when the
 * input cannot form a valid E.164 number.
 */
final class E164Telephone {

    private static final int MIN_DIGITS = 8;
    private static final int MAX_DIGITS = 15;

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
        return "+" + digits;
    }
}
