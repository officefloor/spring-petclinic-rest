package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Strips every non-digit from the built owner's telephone, then requires exactly 10 digits.
 * The normalized value is stored in place, so later steps save and return it as {@code telephone}.
 * A telephone that is not exactly 10 digits is rejected 400 via {@link MissingOwnerFieldsException}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val Owner owner) throws MissingOwnerFieldsException {
        String digits = owner.getTelephone().replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new MissingOwnerFieldsException(List.of("telephone"));
        }
        owner.setTelephone(digits);
    }
}
