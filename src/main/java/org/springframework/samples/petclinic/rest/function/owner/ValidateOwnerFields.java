package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingRequiredFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create request that is missing or blank in any required owner field, reporting every
 * offending field at once. Runs first and binds the body <em>without</em> {@code @Valid} so this rule
 * decides the 400 response shape rather than bean validation. A valid body is republished for
 * {@link BuildOwner} to consume.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingRequiredFieldsException {
        List<String> missing = new ArrayList<>();
        requirePresent(missing, "firstName", request.getFirstName());
        requirePresent(missing, "lastName", request.getLastName());
        requirePresent(missing, "address", request.getAddress());
        requirePresent(missing, "city", request.getCity());
        requirePresent(missing, "telephone", request.getTelephone());
        if (!missing.isEmpty()) {
            throw new MissingRequiredFieldsException(missing);
        }
        validated.set(request);
    }

    private static void requirePresent(List<String> missing, String field, String value) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }
}
