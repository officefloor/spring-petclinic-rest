package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerFieldsRequiredException;
import org.springframework.samples.petclinic.rest.escalation.OwnerTelephoneInvalidException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}. Rejects a request that is missing or blank in any of
 * firstName, lastName, address, city or telephone, collecting every offending field name so the
 * response can list them all. It then normalizes the telephone by removing every non-digit
 * character and requires exactly ten digits, storing that 10-digit value back on the body so it is
 * persisted and returned. On success it publishes the body for {@link BuildOwner} to consume
 * (only one step may bind {@code @RequestBody}).
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsRequiredException, OwnerTelephoneInvalidException {
        List<String> errors = new ArrayList<>();
        require("firstName", request.getFirstName(), errors);
        require("lastName", request.getLastName(), errors);
        require("address", request.getAddress(), errors);
        require("city", request.getCity(), errors);
        require("telephone", request.getTelephone(), errors);
        if (!errors.isEmpty()) {
            throw new OwnerFieldsRequiredException(errors);
        }
        String digits = request.getTelephone().replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new OwnerTelephoneInvalidException(request.getTelephone());
        }
        request.setTelephone(digits);
        validated.set(request);
    }

    private static void require(String field, String value, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(field);
        }
    }
}
