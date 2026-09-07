package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Shared normalization of an owner telephone to E.164 form. Spaces, dashes and brackets are
 * stripped. A leading {@code '+'} and its country code are kept as supplied; otherwise the default
 * country code {@code '+61'} is assumed and a single leading {@code '0'} is dropped from the
 * national digits. The result must carry 8 to 15 digits after the {@code '+'}, or the value is
 * rejected as invalid.
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
        return e164;
    }

    private static InvalidTelephoneException invalid() {
        return new InvalidTelephoneException(
                "Telephone must form a valid E.164 number with 8 to 15 digits after the country code");
    }
}
