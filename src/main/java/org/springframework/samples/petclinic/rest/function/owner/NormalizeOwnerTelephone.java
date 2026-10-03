package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the owner's telephone to E.164 form (see {@link OwnerTelephones}): strip
 * spaces, dashes and brackets; keep a leading '+' and country code when present, otherwise
 * assume country code '+61' and drop a single leading '0' from the national digits; require
 * 8 to 15 digits after the '+', and the exact national-number length for a recognised
 * country code ('+61' => 9, '+1' => 10). The E.164 value is stored back on the owner (so it
 * is persisted and returned); anything that cannot form a valid E.164 number escalates as a
 * 400 via {@link InvalidTelephoneException}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val Owner owner) throws InvalidTelephoneException {
        String e164 = OwnerTelephones.toE164(owner.getTelephone());
        if (e164 == null) {
            throw new InvalidTelephoneException(
                    "Telephone cannot be formatted as a valid E.164 number "
                            + "(8 to 15 digits after the country code, and the national-number "
                            + "length must match the country code: '+61' needs 9 digits, "
                            + "'+1' needs 10)");
        }
        owner.setTelephone(e164);
    }
}
