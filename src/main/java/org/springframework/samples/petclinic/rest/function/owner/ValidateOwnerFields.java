package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}. Rejects a create request that is missing or blank in any
 * required field, so an invalid body is a 400 whose {@code errors} array names each offending field.
 * Runs before {@link BuildOwner}, which then reads the published body via {@code @Val}.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        List<String> errors = new ArrayList<>();
        require(errors, "firstName", request.getFirstName());
        require(errors, "lastName", request.getLastName());
        require(errors, "address", request.getAddress());
        require(errors, "city", request.getCity());
        require(errors, "telephone", request.getTelephone());
        if (!errors.isEmpty()) {
            throw new MissingOwnerFieldsException(errors);
        }
        validated.set(request);
    }

    private static void require(List<String> errors, String field, String value) {
        if (value == null || value.isBlank()) {
            errors.add(field);
        }
    }
}
