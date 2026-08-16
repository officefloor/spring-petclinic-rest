package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerFieldsMissingException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of the create-owner pipeline: rejects a body that is missing or blank in
 * any required field (firstName, lastName, address, city, telephone) with a 400 whose
 * {@code errors} array names each offending field. Runs before {@link BuildOwner}, which
 * reads the published body via {@code @Val}.
 */
public class RequireOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsMissingException {
        List<String> errors = new ArrayList<>();
        checkPresent("firstName", request.getFirstName(), errors);
        checkPresent("lastName", request.getLastName(), errors);
        // Address is required after normalization: a value that is blank once trimmed and
        // collapsed (see NormalizeOwnerAddress) is rejected here, not stored empty.
        if (NormalizeOwnerAddress.normalize(request.getAddress()).isEmpty()) {
            errors.add("address");
        }
        checkPresent("city", request.getCity(), errors);
        checkPresent("telephone", request.getTelephone(), errors);
        if (!errors.isEmpty()) {
            throw new OwnerFieldsMissingException(errors);
        }
        validated.set(request);
    }

    private static void checkPresent(String field, String value, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(field);
        }
    }
}
