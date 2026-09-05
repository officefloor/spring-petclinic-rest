package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Rejects a create-owner request that is missing or blank in any mandatory field, before the body is
 * mapped to an entity. Reads the request published by {@link NormalizeAddress}, so the address has
 * already been normalized and composed. An owner is valid when it supplies an address in either form —
 * a non-blank {@code addressLine1} or the flat {@code address} — so a request blank in both (including
 * blank-after-normalization) is rejected here.
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
        if (isBlank(request.getAddressLine1()) && isBlank(request.getAddress())) {
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
