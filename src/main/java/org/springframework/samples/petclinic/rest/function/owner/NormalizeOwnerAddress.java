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
 *
 * <p>The structured form wins: when {@code addressLine1} is supplied, it and the optional
 * {@code addressLine2} are each normalized and the flat {@code address} is composed from them.
 * Otherwise the flat {@code address} is normalized on its own, so pre-structured minimal owners
 * stay accepted (backward-compatible).
 */
public class NormalizeOwnerAddress {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated) {
        if (OwnerAddresses.isPresent(request.getAddressLine1())) {
            request.setAddressLine1(OwnerAddresses.normalize(request.getAddressLine1()));
            if (request.getAddressLine2() != null) {
                request.setAddressLine2(OwnerAddresses.normalize(request.getAddressLine2()));
            }
            request.setAddress(OwnerAddresses.compose(request.getAddressLine1(), request.getAddressLine2()));
        }
        else {
            request.setAddress(OwnerAddresses.normalize(request.getAddress()));
        }
        validated.set(request);
    }
}
