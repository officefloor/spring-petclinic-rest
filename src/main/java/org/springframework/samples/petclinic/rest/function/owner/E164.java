package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalises a telephone number to E.164 form: keep a leading '+' and country code
 * when present, otherwise assume '+61' and drop a single leading '0' from the national
 * digits. Spaces, dashes and brackets are stripped; 8 to 15 digits must remain.
 */
final class E164 {

    private E164() {
    }

    static String normalize(String telephone) throws InvalidTelephoneException {
        String cleaned = telephone == null ? "" : telephone.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("\\d{8,15}")) {
            throw new InvalidTelephoneException("Telephone cannot be formatted as E.164");
        }
        requireNationalLength(digits);
        return "+" + digits;
    }

    /**
     * Enforce the national-number length required by the country code: '+61' needs 9
     * national digits, '+1' needs 10. Unknown country codes are left unchecked.
     */
    private static void requireNationalLength(String digits) throws InvalidTelephoneException {
        int national = digits.startsWith("61") ? digits.length() - 2
                : digits.startsWith("1") ? digits.length() - 1 : -1;
        int required = digits.startsWith("61") ? 9 : 10;
        if (national >= 0 && national != required) {
            throw new InvalidTelephoneException(
                    "Telephone national number has wrong length for its country code");
        }
    }
}
