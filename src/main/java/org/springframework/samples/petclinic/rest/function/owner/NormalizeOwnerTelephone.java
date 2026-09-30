package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes the owner's telephone on create: strips every non-digit character and then
 * requires exactly 10 digits, mutating the validated request in place so later steps store
 * and return the normalized 10-digit value. Anything that is not exactly 10 digits after
 * stripping is rejected 400 via {@link InvalidTelephoneException}.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String digits = toDigits(request.getTelephone());
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(
                    "Telephone must contain exactly 10 digits after removing non-digit characters");
        }
        request.setTelephone(digits);
    }

    /**
     * The single source of truth for what "same telephone" means: strips every non-digit
     * character, treating {@code null} as empty. Shared with {@link CheckOwnerTelephoneUnique}
     * so the request side and stored owners are normalized identically.
     */
    static String toDigits(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
