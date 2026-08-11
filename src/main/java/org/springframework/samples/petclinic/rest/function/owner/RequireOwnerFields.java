package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}: rejects a request that is missing or blank in any of
 * firstName, lastName, address, city or telephone with a 400 listing every offending field.
 *
 * <p>Binds the body once (no {@code @Valid}, so the missing-field check owns the 400 rather than
 * bean validation) and republishes it as a variable for the later {@link BuildOwner} step.
 */
public class RequireOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        List<String> missing = new ArrayList<>();
        requireText("firstName", request.getFirstName(), missing);
        requireText("lastName", request.getLastName(), missing);
        requireAddress(request.getAddress(), missing);
        requireText("city", request.getCity(), missing);
        requireText("telephone", request.getTelephone(), missing);
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        validated.set(request);
    }

    private static void requireText(String field, String value, List<String> missing) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }

    /**
     * The address is rejected when it is blank <em>after</em> normalization, so a value that
     * collapses to nothing is treated the same as a missing one.
     */
    private static void requireAddress(String value, List<String> missing) {
        if (AddressNormalizer.normalize(value).isEmpty()) {
            missing.add("address");
        }
    }
}
