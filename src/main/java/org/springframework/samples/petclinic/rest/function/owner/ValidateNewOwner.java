package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create request that is missing or blank in any required owner field, before
 * {@link BuildOwner} maps the body to an entity. Runs first so the body is read once here
 * and republished for later steps. The address is judged blank after normalization (see
 * {@link AddressNormalizer}), so an address that collapses to nothing is rejected here.
 */
public class ValidateNewOwner {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        List<String> missing = new ArrayList<>();
        checkPresent("firstName", request.getFirstName(), missing);
        checkPresent("lastName", request.getLastName(), missing);
        checkPresent("address", AddressNormalizer.normalize(request.getAddress()), missing);
        checkPresent("city", request.getCity(), missing);
        checkPresent("telephone", request.getTelephone(), missing);
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        validated.set(request);
    }

    private static void checkPresent(String name, String value, List<String> missing) {
        if (value == null || value.isBlank()) {
            missing.add(name);
        }
    }
}
