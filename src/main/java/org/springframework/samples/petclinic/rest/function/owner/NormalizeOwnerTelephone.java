package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the owner's telephone on create into E.164 form, mutating the validated request in
 * place so later steps store and return the E.164 value. Spaces, dashes and brackets are stripped.
 * A leading '+' and its country code are kept as given; otherwise country code '+61' is assumed and
 * a single leading '0' is dropped from the national digits (so {@code 0412 345 678} becomes
 * {@code +61412345678}). The result must carry 8 to 15 digits after the '+', and its national
 * number (the digits after the country code) must match the length that country requires — '+61'
 * needs 9 national digits, '+1' needs 10. Anything that cannot form valid E.164 is rejected 400 via
 * {@link InvalidTelephoneException}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        request.setTelephone(toE164(request.getTelephone()));
    }

    /**
     * The single source of truth for what "same telephone" means: converts a raw telephone into its
     * E.164 form. Spaces, dashes and brackets are removed; a leading '+' and country code are kept,
     * otherwise '+61' is assumed and a single leading '0' is dropped from the national digits. The
     * digits after the '+' must number 8 to 15, no other characters may remain, and the national
     * number must have the length the country code requires ('+61' => 9, '+1' => 10). The E.164 value
     * feeds the derived {@link org.springframework.samples.petclinic.model.Owner#getIdentityKey()
     * identityKey} so the request side and stored owners are normalized identically.
     *
     * @throws InvalidTelephoneException if the value cannot form valid E.164.
     */
    static String toE164(String telephone) throws InvalidTelephoneException {
        if (telephone == null) {
            throw invalid();
        }
        String cleaned = telephone.replaceAll("[\\s\\-()]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("\\d{8,15}")) {
            throw invalid();
        }
        checkNationalLength(digits);
        return "+" + digits;
    }

    /**
     * Rejects a number whose national number (the digits after the country code) does not match the
     * length that country code requires: '+61' => 9 national digits, '+1' => 10. Country codes not
     * listed here are left to the generic 8-to-15 digit rule.
     */
    private static void checkNationalLength(String digits) throws InvalidTelephoneException {
        String countryCode;
        int requiredNationalLength;
        if (digits.startsWith("61")) {
            countryCode = "61";
            requiredNationalLength = 9;
        }
        else if (digits.startsWith("1")) {
            countryCode = "1";
            requiredNationalLength = 10;
        }
        else {
            return;
        }
        int nationalLength = digits.length() - countryCode.length();
        if (nationalLength != requiredNationalLength) {
            throw new InvalidTelephoneException("Telephone with country code '+" + countryCode
                    + "' must have " + requiredNationalLength + " national digits, but had "
                    + nationalLength);
        }
    }

    private static InvalidTelephoneException invalid() {
        return new InvalidTelephoneException(
                "Telephone must form a valid E.164 number with 8 to 15 digits after the '+'");
    }
}
