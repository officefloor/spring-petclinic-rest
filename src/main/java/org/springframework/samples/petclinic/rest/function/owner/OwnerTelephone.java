package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerTelephoneException;

/**
 * Shared telephone handling for the owner pipelines. Telephone numbers are stored in E.164 form:
 * a leading {@code '+'} and country code are kept when present; otherwise country code {@code +61}
 * is assumed and a single leading {@code '0'} is dropped from the national digits. Spaces, dashes
 * and brackets are stripped, and the result must carry 8 to 15 digits after the {@code '+'}. So
 * {@code "0412 345 678"} is stored as {@code "+61412345678"}. A number that cannot form valid E.164
 * is rejected with {@link InvalidOwnerTelephoneException} (handled as 400).
 */
final class OwnerTelephone {

    /** Assumed country code when the input carries no leading '+'. */
    private static final String DEFAULT_COUNTRY_CODE = "+61";

    /** Spaces, dashes and brackets are removed before parsing. */
    private static final Pattern SEPARATORS = Pattern.compile("[\\s\\-()]");

    private static final Pattern DIGITS = Pattern.compile("\\d+");

    private OwnerTelephone() {
    }

    /**
     * Normalizes the telephone on the request in place to its E.164 form, or throws when the value
     * cannot form a valid E.164 number.
     */
    static void normalize(OwnerFieldsDto request) throws InvalidOwnerTelephoneException {
        request.setTelephone(toE164(request.getTelephone()));
    }

    /**
     * Converts a raw telephone to E.164, or throws {@link InvalidOwnerTelephoneException} when it
     * cannot form a valid E.164 number.
     */
    static String toE164(String raw) throws InvalidOwnerTelephoneException {
        if (raw == null) {
            throw new InvalidOwnerTelephoneException(null);
        }
        String cleaned = SEPARATORS.matcher(raw).replaceAll("");
        String e164;
        if (cleaned.startsWith("+")) {
            // Explicit country code: keep the '+' and everything after it, which must be digits.
            String digits = cleaned.substring(1);
            if (!DIGITS.matcher(digits).matches()) {
                throw new InvalidOwnerTelephoneException(raw);
            }
            e164 = "+" + digits;
        }
        else {
            // No country code: assume +61 and drop a single leading '0' from the national digits.
            if (!DIGITS.matcher(cleaned).matches()) {
                throw new InvalidOwnerTelephoneException(raw);
            }
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            e164 = DEFAULT_COUNTRY_CODE + national;
        }
        int digitCount = e164.length() - 1;
        if (digitCount < 8 || digitCount > 15) {
            throw new InvalidOwnerTelephoneException(raw);
        }
        return e164;
    }

    /**
     * Best-effort E.164 form for comparison, returning {@code null} when the value cannot form a
     * valid E.164 number. Used to compare an existing owner's stored telephone against a request.
     */
    static String toE164OrNull(String raw) {
        try {
            return toE164(raw);
        }
        catch (InvalidOwnerTelephoneException ex) {
            return null;
        }
    }
}
