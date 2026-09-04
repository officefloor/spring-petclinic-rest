package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Strips every non-digit from the create body's telephone, then requires exactly ten
 * digits. Runs before {@link BuildOwner} validates, so the stored value is the bare
 * 10-digit number and anything else is a 400.
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
