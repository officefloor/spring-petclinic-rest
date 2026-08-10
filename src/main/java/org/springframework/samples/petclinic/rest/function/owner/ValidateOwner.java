package org.springframework.samples.petclinic.rest.function.owner;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Validates before {@link LoadOwner} runs, so an invalid body is a 400 even when the owner does not exist.
 * Normalizes the telephone to E.164 (see {@link OwnerTelephone}) so updates are stored in the same
 * form as creates.
 */
@Validated
public class ValidateOwner {

    public void service(@Valid @RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws InvalidEmailException, InvalidTelephoneException {
        request.setTelephone(OwnerTelephone.toE164(request.getTelephone()));
        OwnerEmail.normalize(request);
        validated.set(request);
    }
}
