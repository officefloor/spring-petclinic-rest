package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create request that is missing or blank in any required owner field, so an
 * incomplete body is a 400 whose {@code errors} array names each offending field. Runs
 * first and republishes the body as a variable for {@link BuildOwner}.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingFieldsException {
        List<String> errors = new ArrayList<>();
        if (isBlank(request.getFirstName())) {
            errors.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            errors.add("lastName");
        }
        if (isBlank(request.getAddress())) {
            errors.add("address");
        }
        if (isBlank(request.getCity())) {
            errors.add("city");
        }
        if (isBlank(request.getTelephone())) {
            errors.add("telephone");
        }
        if (!errors.isEmpty()) {
            throw new MissingFieldsException(errors);
        }
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
