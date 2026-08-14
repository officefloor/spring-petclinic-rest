package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the telephone of a create-owner request: strips every non-digit character and requires
 * the result to be exactly ten digits, rejecting anything else with a 400 via
 * {@link InvalidTelephoneException}. Mutates the validated body in place (via {@code @Val}), so the
 * stored and returned {@code telephone} is the 10-digit value. Runs after {@link ValidateOwnerFields}
 * (which guarantees a non-blank telephone) and before {@link BuildOwner}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String telephone = request.getTelephone();
        String digits = telephone == null ? "" : telephone.replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(telephone);
        }
        request.setTelephone(digits);
    }
}
