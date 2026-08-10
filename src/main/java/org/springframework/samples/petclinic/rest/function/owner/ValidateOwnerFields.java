package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerFieldsInvalidException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}: rejects a request that is missing or blank in any of
 * firstName, lastName, address, city or telephone. Throwing lists every offending field, so the
 * client sees them all at once. On success it publishes the body for {@link BuildOwner} to map.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsInvalidException {
        List<String> errors = new ArrayList<>();
        checkPresent("firstName", request.getFirstName(), errors);
        checkPresent("lastName", request.getLastName(), errors);
        checkPresent("address", request.getAddress(), errors);
        checkPresent("city", request.getCity(), errors);
        checkPresent("telephone", request.getTelephone(), errors);
        if (!errors.isEmpty()) {
            throw new OwnerFieldsInvalidException(errors);
        }
        validated.set(request);
    }

    private static void checkPresent(String field, String value, List<String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.add(field);
        }
    }
}
