package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}. Rejects a create whose firstName, lastName,
 * address, city or telephone is missing or blank before {@link BuildOwner} maps the body,
 * so an incomplete request is a 400 listing each field rather than a persisted owner.
 * Also normalizes the telephone by stripping every non-digit character and requires exactly
 * 10 digits, storing the 10-digit value on the body. Publishes the validated body for the
 * later steps.
 */
public class RequireOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidTelephoneException {
        List<String> missing = new ArrayList<>();
        if (isBlank(request.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            missing.add("lastName");
        }
        if (isBlank(request.getAddress())) {
            missing.add("address");
        }
        if (isBlank(request.getCity())) {
            missing.add("city");
        }
        if (isBlank(request.getTelephone())) {
            missing.add("telephone");
        }
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        String digits = request.getTelephone().replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(request.getTelephone());
        }
        request.setTelephone(digits);
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
