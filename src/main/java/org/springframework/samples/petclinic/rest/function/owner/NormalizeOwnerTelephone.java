package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Normalises the create request telephone to E.164 form via {@link E164}. Mutates the published
 * {@link OwnerFieldsDto} in place so {@link BuildOwner} stores the E.164 value. Rejects with 400
 * when the number cannot form a valid E.164.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws MissingOwnerFieldsException {
        String e164 = E164.toE164(request.getTelephone());
        if (e164 == null) {
            throw new MissingOwnerFieldsException(List.of("telephone"));
        }
        request.setTelephone(e164);
    }
}
