package org.springframework.samples.petclinic.rest.function.owner;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerPostcodeException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Validates before {@link LoadOwner} runs, so an invalid body is a 400 even when the owner does not exist.
 */
@Validated
public class ValidateOwner {

    public void service(@Valid @RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws InvalidOwnerEmailException, InvalidOwnerPostcodeException {
        // Normalize the address (structured or flat) and compose the canonical 'address' form.
        OwnerAddress.normalize(request);
        // An owner may include an email; when present it must be valid and is stored lower-cased.
        OwnerEmail.normalize(request);
        // An owner may include a postcode; when present it must be 4 digits valid for the city's region.
        OwnerPostcode.validate(request);
        validated.set(request);
    }
}
