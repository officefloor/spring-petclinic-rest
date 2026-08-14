package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerFieldsValidationException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create-owner request that is missing or blank in any required field
 * (firstName, lastName, address, city, telephone) before {@link BuildOwner} runs.
 *
 * <p>Binds the body here (the single {@code @RequestBody} for the pipeline) and republishes it
 * so later steps read it via {@code @Val}. On failure it throws
 * {@link OwnerFieldsValidationException}, which the escalation handler turns into a 400 whose
 * {@code errors} array lists each offending field.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsValidationException {
        List<String> errors = new ArrayList<>();
        requireText("firstName", request.getFirstName(), errors);
        requireText("lastName", request.getLastName(), errors);
        requireText("address", request.getAddress(), errors);
        requireText("city", request.getCity(), errors);
        requireText("telephone", request.getTelephone(), errors);
        if (!errors.isEmpty()) {
            throw new OwnerFieldsValidationException(errors);
        }
        validated.set(request);
    }

    private static void requireText(String field, String value, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(field);
        }
    }
}
