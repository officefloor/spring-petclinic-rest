package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Runs after {@link ValidateOwnerFields} and before {@link BuildOwner} in the create-owner
 * pipeline. Normalizes the telephone into E.164 form and mutates the validated body in place so
 * later steps build, compare and store the normalized value.
 *
 * <p>Rules: strip spaces, dashes and brackets. A leading {@code '+'} keeps its country code as
 * given; otherwise country code {@code '+61'} is assumed and a single leading {@code '0'} is
 * dropped from the national digits. The result must be 8 to 15 digits after the {@code '+'};
 * anything else (or any leftover non-digit) is rejected with a 400. So {@code '0412 345 678'} is
 * stored as {@code '+61412345678'}.
 *
 * <p>Known country codes additionally fix the national-number length: {@code '+61'} requires 9
 * national digits and {@code '+1'} requires 10. A number whose national part is the wrong length
 * for its country code is rejected with a 400. Other country codes carry no length rule.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String raw = request.getTelephone();
        request.setTelephone(toE164(raw));
    }

    private static String toE164(String raw) throws InvalidTelephoneException {
        if (raw == null) {
            throw new InvalidTelephoneException(raw);
        }
        String trimmed = raw.trim();
        boolean hasCountryCode = trimmed.startsWith("+");
        // Strip formatting: spaces, dashes and brackets.
        String cleaned = trimmed.replaceAll("[\\s\\-()]", "");

        String digits;
        if (hasCountryCode) {
            digits = cleaned.substring(1); // drop the leading '+', keep the country code
        } else {
            String national = cleaned;
            if (national.startsWith("0")) {
                national = national.substring(1); // drop a single leading national-trunk '0'
            }
            digits = "61" + national; // assume Australian country code
        }

        if (!digits.matches("\\d{8,15}")) {
            throw new InvalidTelephoneException(raw);
        }
        requireNationalLength(digits, raw);
        return "+" + digits;
    }

    /**
     * Enforces the national-number length for country codes that fix it: {@code '61'} requires 9
     * national digits, {@code '1'} requires 10. Country codes without a rule pass through.
     */
    private static void requireNationalLength(String digits, String raw)
            throws InvalidTelephoneException {
        int required;
        int nationalLength;
        if (digits.startsWith("61")) {
            required = 9;
            nationalLength = digits.length() - 2;
        } else if (digits.startsWith("1")) {
            required = 10;
            nationalLength = digits.length() - 1;
        } else {
            return; // no length rule for this country code
        }
        if (nationalLength != required) {
            throw new InvalidTelephoneException(raw);
        }
    }
}
