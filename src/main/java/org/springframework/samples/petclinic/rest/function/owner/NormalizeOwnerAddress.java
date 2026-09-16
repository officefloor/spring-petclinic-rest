package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Reads the create-owner body once and normalizes its address via {@link OwnerAddresses} before
 * anything else, then publishes the request for later steps. Running first means the normalized
 * address is what {@link ValidateRequiredOwnerFields} tests for blankness and what the
 * household checks ({@link RejectDuplicateOwnerHousehold}, {@link AssignOwnerHousehold}) compare
 * and persist, so every use of the address sees the same normalized form.
 */
public class NormalizeOwnerAddress {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated) {
        request.setAddress(OwnerAddresses.normalize(request.getAddress()));
        validated.set(request);
    }
}
