package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Rewrites the built owner's telephone into E.164 form (see {@link E164Telephone}) in place, so
 * later steps save and return it as {@code telephone}. A number that cannot form valid E.164 is
 * rejected 400 via {@link MissingOwnerFieldsException}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val Owner owner) throws MissingOwnerFieldsException {
        String e164 = E164Telephone.toE164(owner.getTelephone());
        if (e164 == null) {
            throw new MissingOwnerFieldsException(List.of("telephone"));
        }
        owner.setTelephone(e164);
    }
}
