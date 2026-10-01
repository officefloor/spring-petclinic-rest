package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.model.OwnerRegion;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Validates before {@link LoadOwner} runs, so an invalid body is a 400 even when the owner does not exist.
 */
@Validated
public class ValidateOwner {

    public void service(@Valid @RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsInvalidException {
        // Postcode is optional; when present it must be valid for the city's region (an unknown
        // region accepts any 4-digit postcode), mirroring the create pipeline.
        String postcode = request.getPostcode();
        if (postcode != null && !OwnerRegion.isPostcodeValid(request.getCity(), postcode)) {
            throw new OwnerFieldsInvalidException(List.of("postcode"));
        }
        validated.set(request);
    }
}
