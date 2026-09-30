package org.springframework.samples.petclinic.rest.function.owner;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidAddressException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Validates before {@link LoadOwner} runs, so an invalid body is a 400 even when the owner does not exist.
 *
 * <p>The address may be supplied in either form: the structured {@code addressLine1} (with the
 * optional {@code addressLine2}) or the flat {@code address}. An owner is valid when it supplies an
 * address in either form (a non-blank {@code addressLine1}, or the flat {@code address}) — plus the
 * bean-validated {@code city}. Neither form present is rejected 400 via {@link InvalidAddressException}.
 * The flat {@code address} remains accepted on its own, so earlier minimal owners still pass.
 */
@Validated
public class ValidateOwner {

    public void service(@Valid @RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws InvalidAddressException {
        String address = request.getAddress();
        boolean hasFlat = address != null && !address.isBlank();
        if (!OwnerAddresses.hasStructured(request) && !hasFlat) {
            throw new InvalidAddressException(
                "An address is required: provide addressLine1 or address");
        }
        validated.set(request);
    }
}
