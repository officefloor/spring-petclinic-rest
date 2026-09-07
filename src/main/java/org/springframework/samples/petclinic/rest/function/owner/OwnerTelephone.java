package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Shared normalization of an owner telephone to E.164 form. Spaces, dashes and brackets are
 * stripped. A leading {@code '+'} and its country code are kept as supplied; otherwise the default
 * country code {@code '+61'} is assumed and a single leading {@code '0'} is dropped from the
 * national digits. The result must carry 8 to 15 digits after the {@code '+'}, or the value is
 * rejected as invalid.
 *
 * <p>The national-number length is also checked against the country code: {@code '+61'} requires 9
 * national digits and {@code '+1'} requires 10. A wrong length for the country is rejected.
 *
 * <p>Examples: {@code "0412 345 678" -> "+61412345678"}, {@code "+64 21 123 456" -> "+6421123456"}.
 */
final class OwnerTelephone {

    private OwnerTelephone() {
    }

    /**
     * @return the canonical E.164 telephone (a {@code '+'} followed by 8-15 digits).
     * @throws InvalidTelephoneException when the value cannot form a valid E.164 number.
     */
    static String toE164(String telephone) throws InvalidTelephoneException {
        if (telephone == null) {
            throw invalid();
        }
        // Strip spaces, dashes and brackets; any other non-digit survives and fails the check below.
        String stripped = telephone.replaceAll("[\\s()\\-]", "");
        String e164;
        if (stripped.startsWith("+")) {
            e164 = "+" + stripped.substring(1);
        }
        else {
            String national = stripped;
            if (national.startsWith("0")) {
                national = national.substring(1);
            }
            e164 = "+61" + national;
        }
        String digits = e164.substring(1);
        if (!digits.matches("[0-9]{8,15}")) {
            throw invalid();
        }
        // Per-country national-number length. Only the country codes with a known fixed length are
        // enforced; others keep the generic 8-15 digit rule above.
        if (e164.startsWith("+61")) {
            requireNationalLength(e164, "+61", 9);
        }
        else if (e164.startsWith("+1")) {
            requireNationalLength(e164, "+1", 10);
        }
        return e164;
    }

    private static void requireNationalLength(String e164, String countryCode, int nationalDigits)
            throws InvalidTelephoneException {
        // e164 is '+' + all digits; drop the leading '+' and the country code to get the national part.
        int national = e164.length() - 1 - (countryCode.length() - 1);
        if (national != nationalDigits) {
            throw new InvalidTelephoneException("Telephone with country code " + countryCode
                    + " must have " + nationalDigits + " national digits");
        }
    }

    private static InvalidTelephoneException invalid() {
        return new InvalidTelephoneException(
                "Telephone must form a valid E.164 number with 8 to 15 digits after the country code");
    }
}
