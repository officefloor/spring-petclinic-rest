package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Normalises the create request telephone by stripping every non-digit character, then requires
 * exactly ten digits. Mutates the published {@link OwnerFieldsDto} in place so {@link BuildOwner}
 * stores the digits-only value. Rejects with 400 when the stripped value is not ten digits.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws MissingOwnerFieldsException {
        String digits = request.getTelephone().replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new MissingOwnerFieldsException(List.of("telephone"));
        }
        request.setTelephone(digits);
    }
}
