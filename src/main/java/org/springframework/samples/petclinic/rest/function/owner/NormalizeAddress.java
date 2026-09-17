package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Normalizes the address of a create request to its canonical form (see
 * {@link AddressNormalizer#normalize(String)}) before any other rule runs. Running first means the
 * required-field check ({@link ValidateOwnerFields}) rejects an address that is blank after
 * normalization, the household comparisons ({@link EnsureUniqueHousehold}, {@link AssignHousehold})
 * compare normalized addresses, and {@link BuildOwner} stores — and later responses return — the
 * normalized form. Binds the request body once and republishes it for the rest of the pipeline.
 */
public class NormalizeAddress {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> normalized) {
        request.setAddress(AddressNormalizer.normalize(request.getAddress()));
        normalized.set(request);
    }
}
