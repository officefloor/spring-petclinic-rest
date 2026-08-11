package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the create-owner telephone into E.164 form and writes it back onto the body so
 * {@link BuildOwner} maps and {@link SaveOwner} stores the canonical value. A leading '+'
 * and country code are kept when present; otherwise country code '+61' is assumed and a
 * single leading '0' is dropped from the national digits. Spaces, dashes and brackets are
 * stripped. A number that cannot form a valid E.164 string (8 to 15 digits after the '+')
 * raises {@link InvalidTelephoneException} (400).
 *
 * <p>Runs after {@link ValidateOwnerFields} (which rejects a blank telephone) and mutates the
 * validated body in place — {@code @Val} yields the same object the earlier step published.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String e164 = TelephoneE164.toE164(request.getTelephone());
        if (!TelephoneE164.isValid(e164)) {
            throw new InvalidTelephoneException(e164 == null ? "" : e164);
        }
        request.setTelephone(e164);
    }
}
