package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Converts a raw telephone into E.164 form: a leading '+' and country code are kept when
 * present, otherwise country code '+61' is assumed and a single leading '0' is dropped from
 * the national digits. Spaces, dashes and brackets (indeed every non-digit character) are
 * stripped, and the result must carry 8 to 15 digits after the '+'.
 *
 * <p>A plain utility (not an OfficeFloor function), so it may expose helpers without
 * tripping the one-public-method-per-function rule.
 */
public final class E164Telephone {

    private E164Telephone() {
    }

    /**
     * @return the E.164 form ('+' followed by 8 to 15 digits).
     * @throws InvalidTelephoneException if the input cannot form valid E.164.
     */
    public static String normalize(String raw) throws InvalidTelephoneException {
        if (raw == null) {
            throw new InvalidTelephoneException("Telephone is required");
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
            throw new InvalidTelephoneException(
                    "Telephone must have 8 to 15 digits in E.164 form");
        }
        return "+" + digits;
    }

    /** E.164 form of a telephone, or {@code null} when it is absent or cannot be formed. */
    public static String normalizeOrNull(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return normalize(raw);
        }
        catch (InvalidTelephoneException ex) {
            return null;
        }
    }
}
