package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create request that omits or blanks any required owner field, before the body is
 * mapped, so each missing field becomes a 400 listing its name.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingFieldsException {
        List<String> errors = new ArrayList<>();
        requireField(errors, "firstName", request.getFirstName());
        requireField(errors, "lastName", request.getLastName());
        requireField(errors, "address", request.getAddress());
        requireField(errors, "city", request.getCity());
        requireField(errors, "telephone", request.getTelephone());
        if (!errors.isEmpty()) {
            throw new MissingFieldsException(errors);
        }
        validated.set(request);
    }

    private static void requireField(List<String> errors, String name, String value) {
        if (value == null || value.isBlank()) {
            errors.add(name);
        }
    }
}
