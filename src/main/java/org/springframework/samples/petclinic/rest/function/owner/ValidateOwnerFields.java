package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Runs first in the create-owner pipeline. Rejects a request that is missing or blank in any of
 * firstName, lastName, address, city or telephone with a 400 naming each offending field, before
 * {@link BuildOwner} runs. Publishes the validated body for later steps as they must not bind the
 * request body a second time.
 *
 * <p>The address is {@link OwnerAddresses#normalize normalized} in place before it is checked and
 * published, so an address that is blank only after normalization is rejected here and every later
 * step (build, household comparison, store/return) works with the single normalized form.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        request.setAddress(OwnerAddresses.normalize(request.getAddress()));

        List<String> missing = new ArrayList<>();
        require("firstName", request.getFirstName(), missing);
        require("lastName", request.getLastName(), missing);
        require("address", request.getAddress(), missing);
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
}
