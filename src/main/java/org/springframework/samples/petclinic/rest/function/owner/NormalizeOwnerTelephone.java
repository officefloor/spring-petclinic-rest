package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * On create, rewrites the telephone into E.164 form. A leading '+' keeps the given
 * country code; otherwise '+61' is assumed and a single leading '0' is dropped from the
 * national digits. Spaces, dashes and brackets are stripped, and 8 to 15 digits must
 * follow the '+'. Mutates the built {@link Owner} in place so later steps store and
 * return the E.164 value.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val Owner owner) throws InvalidTelephoneException {
        String raw = owner.getTelephone() == null ? "" : owner.getTelephone().trim();
        String digits = raw.replaceAll("[^0-9]", "");
        String e164;
        if (raw.startsWith("+")) {
            e164 = "+" + digits;
        } else {
            e164 = "+61" + (digits.startsWith("0") ? digits.substring(1) : digits);
        }
        int count = e164.length() - 1;
        if (count < 8 || count > 15) {
            throw new InvalidTelephoneException("Telephone cannot form a valid E.164 number");
        }
        owner.setTelephone(e164);
    }
}
