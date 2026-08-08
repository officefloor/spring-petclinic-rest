package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes a new owner's telephone to E.164 form: strips spaces, dashes and brackets, keeps a
 * leading '+' and country code when present, otherwise assumes country code '+61' and drops a
 * single leading '0' from the national digits, and requires 8 to 15 digits after the '+'. So
 * '0412 345 678' is stored as '+61412345678'. The stored (and later returned) value is the E.164
 * string; anything that cannot form valid E.164 is a 400. Mutates the built {@link Owner} in place
 * so {@link SaveOwner} persists the normalized value.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val Owner owner) throws InvalidTelephoneException {
        String e164 = E164Telephone.toE164OrNull(owner.getTelephone());
        if (e164 == null) {
            throw new InvalidTelephoneException(
                    "Telephone must form a valid E.164 number with 8 to 15 digits after the '+'");
        }
        owner.setTelephone(e164);
    }
}
