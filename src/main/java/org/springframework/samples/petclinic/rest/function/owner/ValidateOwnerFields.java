package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerFieldsInvalidException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create request that is missing or blank in any required owner field, before
 * {@link BuildOwner} runs. Reads the body once and republishes it for later steps.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsInvalidException {
        List<String> errors = new ArrayList<>();
        checkRequired("firstName", request.getFirstName(), errors);
        checkRequired("lastName", request.getLastName(), errors);
        checkRequired("address", request.getAddress(), errors);
        checkRequired("city", request.getCity(), errors);
        checkRequired("telephone", request.getTelephone(), errors);
        if (!errors.isEmpty()) {
            throw new OwnerFieldsInvalidException(errors);
        }
        validated.set(request);
    }

    private static void checkRequired(String field, String value, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(field);
        }
    }
}
