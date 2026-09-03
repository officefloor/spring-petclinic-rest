package org.springframework.samples.petclinic.rest.function.owner;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Validates before {@link LoadOwner} runs, so an invalid body is a 400 even when the owner does not exist.
 * The telephone is normalized to E.164 form (see {@link OwnerTelephone}); a value that cannot form valid
 * E.164 is a 400. A present-but-invalid email is a 400 here; a valid one is normalized to lower case for
 * {@link ApplyOwner}.
 */
@Validated
public class ValidateOwner {

    public void service(@Valid @RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws InvalidEmailException, DisposableEmailException, InvalidTelephoneException,
            InvalidPostcodeException {
        String line1 = OwnerAddress.normalize(request.getAddressLine1());
        String line2 = OwnerAddress.normalize(request.getAddressLine2());
        request.setAddressLine1(line1.isEmpty() ? null : line1);
        request.setAddressLine2(line2.isEmpty() ? null : line2);
        request.setAddress(OwnerAddress.compose(request.getAddressLine1(),
                request.getAddressLine2(), request.getAddress()));
        request.setTelephone(OwnerTelephone.toE164(request.getTelephone()));
        request.setEmail(OwnerEmail.normalize(request.getEmail()));
        request.setPostcode(Postcode.normalize(request.getPostcode(), request.getCity()));
        validated.set(request);
    }
}
