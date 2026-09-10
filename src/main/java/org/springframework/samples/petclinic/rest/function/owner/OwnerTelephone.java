package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Normalizes an owner telephone number into E.164 form.
 *
 * <p>Rules: strip spaces, dashes and brackets. If a leading {@code '+'} (and therefore an
 * explicit country code) is present it is kept as-is. Otherwise the default country code
 * {@code +61} is assumed and a single leading {@code '0'} is dropped from the national digits.
 * The result must carry 8 to 15 digits after the {@code '+'}. Anything that cannot form a
 * valid E.164 number raises {@link InvalidOwnerTelephoneException} so the endpoint responds 400.
 *
 * <p>So {@code "0412 345 678"} becomes {@code "+61412345678"} and {@code "+64 21 123 456"}
 * becomes {@code "+6421123456"}.
 */
final class OwnerTelephone {

    /** Default country code assumed when the input carries no explicit {@code '+'} prefix. */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    private static final int MIN_DIGITS = 8;

    private static final int MAX_DIGITS = 15;

    private OwnerTelephone() {
    }

    /**
     * Convert a raw telephone string into E.164 form, or throw when it cannot form a valid one.
     */
    static String toE164(String raw) throws InvalidOwnerTelephoneException {
        if (raw == null) {
            throw new InvalidOwnerTelephoneException("null");
        }
        // Strip spaces, dashes and brackets.
        String cleaned = raw.replaceAll("[\\s()\\[\\]-]", "");
        boolean explicitCountryCode = cleaned.startsWith("+");
        String digits = explicitCountryCode ? cleaned.substring(1) : cleaned;
        if (digits.isEmpty() || !digits.chars().allMatch(Character::isDigit)) {
            throw new InvalidOwnerTelephoneException(raw);
        }
        String e164Digits;
        if (explicitCountryCode) {
            e164Digits = digits;
        }
        else {
            // No explicit country code: assume the default and drop one leading national '0'.
            String national = digits.startsWith("0") ? digits.substring(1) : digits;
            e164Digits = DEFAULT_COUNTRY_CODE + national;
        }
        if (e164Digits.length() < MIN_DIGITS || e164Digits.length() > MAX_DIGITS) {
            throw new InvalidOwnerTelephoneException(raw);
        }
        return "+" + e164Digits;
    }

    /**
     * Canonical E.164 form for comparison, falling back to a digits-only form for values that
     * cannot form a valid E.164 number (e.g. legacy data).
     */
    static String canonical(String raw) {
        try {
            return toE164(raw);
        }
        catch (InvalidOwnerTelephoneException ex) {
            return raw == null ? "" : raw.replaceAll("\\D", "");
        }
    }
}
