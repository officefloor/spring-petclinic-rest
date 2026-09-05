package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Normalizes a create-owner request's {@code address} to its canonical form (see
 * {@link AddressNormalizer}: trim, collapse whitespace, upper-case, expand common abbreviations) before
 * any other step runs. Binds the request body (the single {@code @RequestBody} step of this pipeline)
 * and republishes it as a variable for the later steps. Because it runs first, the normalized value is
 * what {@link RequireOwnerFields} tests for blankness, what the household component of the derived
 * {@link IdentityKey} is built from, and what {@link BuildOwner} maps to the entity — so a blank-after-normalization address is rejected
 * and the normalized {@code address} is stored and returned.
 */
public class NormalizeAddress {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> normalized) {
        request.setAddress(AddressNormalizer.normalize(request.getAddress()));
        normalized.set(request);
    }
}
