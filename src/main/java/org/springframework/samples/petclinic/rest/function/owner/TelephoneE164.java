package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Shared normalization for an owner's {@code telephone} to E.164 form. Spaces, dashes and
 * brackets are stripped. A leading '+' and country code are kept when present; otherwise the
 * country code '+61' is assumed and a single leading '0' is dropped from the national digits.
 * The result must have 8 to 15 digits after the '+'. So {@code "0412 345 678"} becomes {@code
 * "+61412345678"}. A number that cannot form valid E.164 is rejected with 400 via {@link
 * InvalidTelephoneException}.
 */
final class TelephoneE164 {

    /** Characters removed before interpreting the number. */
    private static final Pattern SEPARATORS = Pattern.compile("[\\s\\-()]");

    /** 8 to 15 digits, the E.164 length bound (country code plus national digits). */
    private static final Pattern E164_DIGITS = Pattern.compile("\\d{8,15}");

    private TelephoneE164() {
    }

    /**
     * Returns the E.164 string (a '+' followed by 8 to 15 digits) for the given raw telephone.
     *
     * @throws InvalidTelephoneException when no valid E.164 string can be formed.
     */
    static String normalize(String telephone) throws InvalidTelephoneException {
        if (telephone == null) {
            throw new InvalidTelephoneException(null);
        }
        String cleaned = SEPARATORS.matcher(telephone).replaceAll("");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!E164_DIGITS.matcher(digits).matches()) {
            throw new InvalidTelephoneException(telephone);
        }
        return "+" + digits;
    }
}
