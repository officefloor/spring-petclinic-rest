package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Normalizes a telephone number to <a href="https://en.wikipedia.org/wiki/E.164">E.164</a>
 * form: a leading {@code '+'} followed by 8 to 15 digits.
 *
 * <p>Rules:
 * <ul>
 *   <li>spaces, dashes and brackets are stripped;</li>
 *   <li>when the input already carries a leading {@code '+'} (a country code), it is kept
 *       and the remaining digits are used verbatim;</li>
 *   <li>otherwise country code {@code '+61'} is assumed and a single leading {@code '0'} is
 *       dropped from the national digits;</li>
 *   <li>the result must have 8 to 15 digits after the {@code '+'}, else it is not valid E.164;</li>
 *   <li>the national-number length must match the country code — {@code '+61'} requires exactly 9
 *       national digits and {@code '+1'} requires exactly 10; a wrong length is not valid.</li>
 * </ul>
 *
 * <p>So {@code "0412 345 678"} becomes {@code "+61412345678"} and {@code "+64 21 123 456"}
 * becomes {@code "+6421123456"}.
 */
public final class TelephoneE164 {

    private TelephoneE164() {
    }

    /**
     * Returns the E.164 form of {@code raw}, or {@code null} if it cannot form a valid E.164
     * number.
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        boolean hasCountryCode = raw.strip().startsWith("+");
        String digits = raw.replaceAll("\\D", "");
        String e164Digits;
        if (hasCountryCode) {
            e164Digits = digits;
        }
        else {
            String national = digits.startsWith("0") ? digits.substring(1) : digits;
            e164Digits = "61" + national;
        }
        if (e164Digits.length() < 8 || e164Digits.length() > 15) {
            return null;
        }
        if (!hasValidNationalLength(e164Digits)) {
            return null;
        }
        return "+" + e164Digits;
    }

    /**
     * Checks the national-number length against the country code. {@code '+61'} requires exactly 9
     * national digits and {@code '+1'} requires exactly 10; other country codes are not constrained
     * here beyond the overall 8-to-15 E.164 length.
     */
    private static boolean hasValidNationalLength(String e164Digits) {
        if (e164Digits.startsWith("61")) {
            return e164Digits.length() - 2 == 9;
        }
        if (e164Digits.startsWith("1")) {
            return e164Digits.length() - 1 == 10;
        }
        return true;
    }
}
