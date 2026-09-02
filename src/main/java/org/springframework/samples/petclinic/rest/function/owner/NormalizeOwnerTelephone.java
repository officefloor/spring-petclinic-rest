package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Strips every non-digit from the create request's telephone and requires exactly 10 digits,
 * storing the normalized value in place. Runs before {@link BuildOwner}, so the entity and the
 * response both carry the 10-digit form; too few (or too many) digits reject with 400.
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
