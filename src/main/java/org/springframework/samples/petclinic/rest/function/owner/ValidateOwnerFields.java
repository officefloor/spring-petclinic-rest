package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingRequiredFieldsException;

/**
 * Rejects a create request that is missing or blank in any required owner field, reporting every
 * offending field at once. Reads the request <em>without</em> {@code @Valid} so this rule decides the
 * 400 response shape rather than bean validation. Runs after {@link NormalizeAddress}, so the address
 * is judged in its normalized form and a value that is blank after normalization is rejected.
 */
public class ValidateOwnerFields {

    public void service(@Val OwnerFieldsDto request)
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
    }

    private static void requirePresent(List<String> missing, String field, String value) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }
}
