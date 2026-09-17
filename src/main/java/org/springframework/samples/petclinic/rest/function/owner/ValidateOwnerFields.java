package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}: rejects a body that is missing or blank in any
 * required field, reporting every offending field at once. Publishes the validated body
 * for {@link BuildOwner} so the request is read only here.
 */
public class ValidateOwnerFields {

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

    // An address must be supplied in one form or the other: a non-blank structured
    // 'addressLine1', or the flat 'address' (blank once normalized, e.g. only whitespace,
    // does not count).
    private static void requireAddress(OwnerFieldsDto request, List<String> missing) {
        boolean structured = !AddressNormalizer.isBlank(request.getAddressLine1());
        boolean flat = !AddressNormalizer.normalize(request.getAddress()).isEmpty();
        if (!structured && !flat) {
            missing.add("address");
        }
    }

    private static void require(String name, String value, List<String> missing) {
        if (value == null || value.isBlank()) {
            missing.add(name);
        }
    }
}
