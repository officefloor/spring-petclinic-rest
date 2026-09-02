package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create request that is missing or blank in any mandatory owner field, before the body
 * is mapped to an entity. Publishes the body for {@link BuildOwner} once every field is present.
 */
public class RequireOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        List<String> missing = new ArrayList<>();
        require("firstName", request.getFirstName(), missing);
        require("lastName", request.getLastName(), missing);
        if (isBlank(request.getAddressLine1()) && isBlank(request.getAddress())) {
            missing.add("address");
        }
        require("city", request.getCity(), missing);
        require("telephone", request.getTelephone(), missing);
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        validated.set(request);
    }

    private void require(String name, String value, List<String> missing) {
        if (isBlank(value)) {
            missing.add(name);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
