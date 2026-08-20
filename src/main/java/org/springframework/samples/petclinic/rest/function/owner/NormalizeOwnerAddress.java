package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of create-owner: reads the request body once, normalizes the address into its
 * canonical form (see {@link AddressNormalizer}) in place, and republishes the body for later
 * steps, so no later step binds {@code @RequestBody} again. Running before {@link ValidateOwnerFields}
 * means the required-field check sees the normalized address and rejects one that is blank after
 * normalization, and every downstream comparison (household duplicate detection, shared household id)
 * works from the stored, normalized value.
 */
public class NormalizeOwnerAddress {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> body) {
        request.setAddress(AddressNormalizer.normalize(request.getAddress()));
        body.set(request);
    }
}
