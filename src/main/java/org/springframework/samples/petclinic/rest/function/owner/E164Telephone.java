package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Converts a raw telephone into E.164 form. Spaces, dashes and brackets are stripped. A leading
 * '+' with its country code is kept; otherwise country code '+61' is assumed and a single leading
 * '0' is dropped from the national digits. The result must carry 8 to 15 digits after the '+',
 * else it is rejected with {@link InvalidTelephoneException} (a 400). So {@code '0412 345 678'}
 * becomes {@code '+61412345678'} and {@code '+64 21 123 456'} becomes {@code '+6421123456'}.
 *
 * <p>The national-number length is additionally checked against known country codes: {@code '+61'}
 * requires exactly 9 national digits and {@code '+1'} requires exactly 10. A number whose national
 * length is wrong for its country is rejected with {@link InvalidTelephoneException} (a 400). Other
 * country codes are only bound by the general 8-to-15 total-digit rule.
 */
final class E164Telephone {

    private E164Telephone() {
    }

    /** Normalize to E.164, throwing when the value cannot form a valid number. */
    static String toE164(String raw) throws InvalidTelephoneException {
        String cleaned = raw == null ? "" : raw.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("\\d{8,15}")) {
            throw new InvalidTelephoneException(raw);
        }
        // National-number length must match the country code.
        if (digits.startsWith("61")) {
            if (digits.length() - 2 != 9) {
                throw new InvalidTelephoneException(raw);
            }
        }
        else if (digits.startsWith("1")) {
            if (digits.length() - 1 != 10) {
                throw new InvalidTelephoneException(raw);
            }
        }
        return "+" + digits;
    }

    /** Normalize to E.164, or {@code null} when the value cannot form a valid number. */
    static String toE164OrNull(String raw) {
        try {
            return toE164(raw);
        }
        catch (InvalidTelephoneException ex) {
            return null;
        }
    }
}
