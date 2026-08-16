package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerTelephoneInvalidException;

/**
 * Normalizes the telephone of the validated body (published by {@link RequireOwnerFields})
 * into E.164 form: spaces, dashes and brackets are stripped; a leading {@code '+'} with its
 * country code is kept when present, otherwise country code {@code '+61'} is assumed and a
 * single leading {@code '0'} is dropped from the national digits. The result must have 8 to
 * 15 digits after the {@code '+'}. The E.164 value is written back onto the body in place, so
 * {@link BuildOwner} maps it and later reads/responses return it. A telephone that cannot form
 * a valid E.164 number is rejected 400 via {@link OwnerTelephoneInvalidException}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws OwnerTelephoneInvalidException {
        String raw = request.getTelephone();
        String e164 = toE164(raw);
        if (e164 == null) {
            throw new OwnerTelephoneInvalidException(raw == null ? "" : raw);
        }
        request.setTelephone(e164);
    }

    /**
     * Converts a telephone into E.164 form, or returns {@code null} when it cannot form a
     * valid E.164 number. Shared with {@link RequireUniqueOwnerTelephone} so stored and new
     * telephones are compared in the same canonical form.
     */
    static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        // Strip spaces, dashes and brackets (leave a leading '+' and the digits).
        String cleaned = raw.replaceAll("[\\s\\-()]", "").trim();
        String digits;
        if (cleaned.startsWith("+")) {
            // Explicit country code: keep the digits following the '+'.
            digits = cleaned.substring(1);
        } else {
            // No country code: assume '+61', dropping a single leading '0'.
            String national = cleaned;
            if (national.startsWith("0")) {
                national = national.substring(1);
            }
            digits = "61" + national;
        }
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        return "+" + digits;
    }
}
