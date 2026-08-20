package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerTelephoneException;

/**
 * Normalizes the create-owner telephone: strips every non-digit character and requires
 * exactly ten digits. Mutates the published request in place so later steps store and
 * return the 10-digit value. Rejects anything that is not exactly ten digits with a 400.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidOwnerTelephoneException {
        String telephone = request.getTelephone();
        String digits = telephone == null ? "" : telephone.replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidOwnerTelephoneException(telephone);
        }
        request.setTelephone(digits);
    }
}
