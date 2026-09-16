package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Rejects a create-owner request that is missing or blank in any required field, before
 * {@link BuildOwner} runs. Reads the request already published by {@link NormalizeOwnerAddress},
 * so the address it tests for blankness is the normalized form.
 *
 * <p>An address may be supplied in either form: a non-blank structured {@code addressLine1} or
 * the flat {@code address}. Only when both are blank is {@code address} reported missing, so
 * earlier minimal (flat-address) owners stay accepted.
 */
public class ValidateRequiredOwnerFields {

    public void service(@Val OwnerFieldsDto request) throws MissingOwnerFieldsException {
        List<String> missing = new ArrayList<>();
        require("firstName", request.getFirstName(), missing);
        require("lastName", request.getLastName(), missing);
        if (!OwnerAddresses.isPresent(request.getAddressLine1())
                && !OwnerAddresses.isPresent(request.getAddress())) {
            missing.add("address");
        }
        require("city", request.getCity(), missing);
        require("telephone", request.getTelephone(), missing);
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
    }

    private static void require(String field, String value, List<String> missing) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }
}
