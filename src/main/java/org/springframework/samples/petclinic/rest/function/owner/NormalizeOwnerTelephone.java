package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-time normalisation for {@code POST /owners}: strips every non-digit character from the
 * telephone and requires exactly ten digits to remain. The normalised 10-digit value is written
 * back onto the body in place (the {@code @Val} object is the same instance {@link BuildOwner}
 * then maps), so it is what gets stored and returned. A value that is not exactly ten digits
 * after stripping is rejected with 400 via {@link InvalidTelephoneException}.
 *
 * <p>Runs after {@link ValidateOwnerFields} (which has already guaranteed telephone is present
 * and non-blank) and before {@link BuildOwner}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String original = request.getTelephone();
        String digits = original == null ? "" : original.replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(original);
        }
        request.setTelephone(digits);
    }
}
