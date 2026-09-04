package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create-owner body that is missing or blank in any required field, before
 * {@link BuildOwner} maps it, so the response names each offending field.
 */
public class RequireOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        List<String> missing = new ArrayList<>();
        require("firstName", request.getFirstName(), missing);
        require("lastName", request.getLastName(), missing);
        requireAddress(request, missing);
        require("city", request.getCity(), missing);
        require("telephone", request.getTelephone(), missing);
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        validated.set(request);
    }

    private static void require(String name, String value, List<String> missing) {
        if (value == null || value.isBlank()) {
            missing.add(name);
        }
    }

    /** An address is supplied in either form: a non-blank structured addressLine1, or the flat address. */
    private static void requireAddress(OwnerFieldsDto request, List<String> missing) {
        boolean structured = request.getAddressLine1() != null && !request.getAddressLine1().isBlank();
        boolean flat = request.getAddress() != null && !request.getAddress().isBlank();
        if (!structured && !flat) {
            missing.add("address");
        }
    }
}
