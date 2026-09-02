package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Rewrites the create request's telephone into E.164 form, storing it in place. Spaces, dashes and
 * brackets are stripped; a leading '+' and country code are kept, otherwise country code '+61' is
 * assumed and a single leading '0' is dropped from the national digits. Runs before {@link BuildOwner},
 * so the entity and the response both carry the E.164 form; anything that cannot yield 8 to 15 digits
 * after the '+' rejects with 400.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws MissingOwnerFieldsException {
        String cleaned = request.getTelephone().replaceAll("[\\s()\\-]", "");
        String e164;
        if (cleaned.startsWith("+")) {
            e164 = cleaned;
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            e164 = "+61" + national;
        }
        if (!e164.substring(1).matches("\\d{8,15}")) {
            throw new MissingOwnerFieldsException(List.of("telephone"));
        }
        request.setTelephone(e164);
    }
}
