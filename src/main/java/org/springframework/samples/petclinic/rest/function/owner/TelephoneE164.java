package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes telephone numbers to E.164 form.
 *
 * <p>Rules: strip spaces, dashes and brackets; keep a leading '+' and its country code when
 * present; otherwise assume country code '+61' and drop a single leading '0' from the national
 * digits. The result is '+' followed by 8 to 15 digits, so '0412 345 678' becomes
 * '+61412345678' and '+64 21 123 456' becomes '+6421123456'.
 *
 * <p>Country-specific national-number length: '+61' requires exactly 9 national digits and '+1'
 * requires exactly 10; a number whose national part is the wrong length for its country code is
 * rejected. Other country codes are only bound by the general 8-to-15 total-digit rule.
 */
public final class TelephoneE164 {

    private TelephoneE164() {
    }

    /**
     * Converts to E.164, throwing {@link InvalidTelephoneException} when the input cannot form a
     * valid E.164 number.
     */
    public static String normalize(String input) throws InvalidTelephoneException {
        String e164 = toE164(input);
        if (e164 == null) {
            throw new InvalidTelephoneException(input);
        }
        return e164;
    }

    /**
     * Best-effort conversion to E.164; returns {@code null} when the input cannot form a valid
     * E.164 number.
     */
    public static String toE164(String input) {
        if (input == null) {
            return null;
        }
        String cleaned = input.replaceAll("[\\s\\-()]", "");
        boolean hasPlus = cleaned.startsWith("+");
        String digits = hasPlus ? cleaned.substring(1) : cleaned;
        if (digits.isEmpty() || !digits.chars().allMatch(c -> c >= '0' && c <= '9')) {
            return null;
        }
        String e164;
        if (hasPlus) {
            e164 = digits;
        }
        else {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            e164 = "61" + digits;
        }
        if (e164.length() < 8 || e164.length() > 15) {
            return null;
        }
        if (!hasValidNationalLength(e164)) {
            return null;
        }
        return "+" + e164;
    }

    /**
     * Checks the national-number length for country codes with a fixed requirement: '+61' must
     * have 9 national digits and '+1' must have 10. Any other country code passes (its length is
     * only bound by the general 8-to-15 total-digit rule).
     */
    private static boolean hasValidNationalLength(String e164) {
        if (e164.startsWith("61")) {
            return e164.length() - 2 == 9;
        }
        if (e164.startsWith("1")) {
            return e164.length() - 1 == 10;
        }
        return true;
    }
}
