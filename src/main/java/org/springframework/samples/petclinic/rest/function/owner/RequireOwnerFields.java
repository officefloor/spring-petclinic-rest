package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Rejects a create-owner request that is missing or blank in any mandatory field, before the body is
 * mapped to an entity. Reads the request published by {@link NormalizeAddress}, so the {@code address}
 * has already been normalized: a blank-after-normalization address is rejected here.
 */
public class RequireOwnerFields {

    public void service(@Val OwnerFieldsDto request) throws MissingOwnerFieldsException {
        List<String> missing = new ArrayList<>();
        if (isBlank(request.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            missing.add("lastName");
        }
        if (isBlank(request.getAddress())) {
            missing.add("address");
        }
        if (isBlank(request.getCity())) {
            missing.add("city");
        }
        if (isBlank(request.getTelephone())) {
            missing.add("telephone");
        }
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
