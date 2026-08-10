package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingRequiredFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Runs first in the create-owner pipeline. Rejects a request that is missing or blank in any of
 * firstName, lastName, address, city or telephone by throwing {@link MissingRequiredFieldsException}
 * (handled as 400). Binds the body (without {@code @Valid}) so a blank/missing field is reported as a
 * required-field error rather than a bean-validation error, then republishes it for {@link BuildOwner}.
 *
 * <p>The address is {@link AddressNormalizer normalized} (trimmed, whitespace-collapsed, upper-cased,
 * abbreviations expanded) before it is checked, so an address that is blank <em>after</em> normalization
 * is rejected, and the normalized value is written back so downstream steps store, return and compare it
 * in that one canonical form.
 */
public class ValidateRequiredOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingRequiredFieldsException {
        String address = AddressNormalizer.normalize(request.getAddress());
        List<String> missing = new ArrayList<>();
        addIfBlank(missing, "firstName", request.getFirstName());
        addIfBlank(missing, "lastName", request.getLastName());
        addIfBlank(missing, "address", address);
        addIfBlank(missing, "city", request.getCity());
        addIfBlank(missing, "telephone", request.getTelephone());
        if (!missing.isEmpty()) {
            throw new MissingRequiredFieldsException(missing);
        }
        request.setAddress(address);
        validated.set(request);
    }

    private static void addIfBlank(List<String> missing, String field, String value) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }
}
