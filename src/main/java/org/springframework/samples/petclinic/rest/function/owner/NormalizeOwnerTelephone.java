package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the create-owner telephone: strips every non-digit character, requires the
 * result to be exactly ten digits, then writes that ten-digit value back onto the body so
 * {@link BuildOwner} maps and {@link SaveOwner} stores the canonical form. A telephone that
 * is not exactly ten digits after stripping raises {@link InvalidTelephoneException} (400).
 *
 * <p>Runs after {@link ValidateOwnerFields} (which rejects a blank telephone) and mutates the
 * validated body in place — {@code @Val} yields the same object the earlier step published.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String telephone = request.getTelephone();
        String digits = telephone == null ? "" : telephone.replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(digits);
        }
        request.setTelephone(digits);
    }
}
