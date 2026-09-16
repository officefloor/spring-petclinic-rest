package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Canonicalizes the create-owner address to its normalized form (see {@link OwnerAddresses})
 * and publishes the request body for the rest of the pipeline. Runs first so the normalized
 * address flows into the required-field check, the household comparisons, storage and the
 * response.
 *
 * <p>Binds the request body once for the pipeline; later steps read it via {@code @Val}.
 */
public class NormalizeOwnerAddress {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> body) {
        request.setAddress(OwnerAddresses.normalize(request.getAddress()));
        body.set(request);
    }
}
