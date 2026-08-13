package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Runs after {@link RequireOwnerFields} on {@code POST /api/owners}: normalizes the telephone into
 * E.164 form, replacing the field in place (so {@link BuildOwner} maps the normalized value).
 * <p>
 * Spaces, dashes and brackets are stripped. When the number carries a leading {@code '+'} the
 * country code is kept as given; otherwise country code {@code '+61'} is assumed and a single
 * leading {@code '0'} is dropped from the national digits. The result must have 8 to 15 digits
 * after the {@code '+'}. A number that cannot form a valid E.164 string is rejected with 400 via
 * {@link InvalidTelephoneException}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String raw = request.getTelephone();
        String stripped = raw == null ? "" : raw.replaceAll("[\\s\\-()]", "");

        String digits;
        if (stripped.startsWith("+")) {
            digits = stripped.substring(1);
        }
        else {
            String national = stripped.startsWith("0") ? stripped.substring(1) : stripped;
            digits = "61" + national;
        }

        if (!digits.matches("\\d{8,15}")) {
            throw new InvalidTelephoneException(raw);
        }
        request.setTelephone("+" + digits);
    }
}
