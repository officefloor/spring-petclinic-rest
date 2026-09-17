package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Resolves the address of a create request to its canonical form (see
 * {@link AddressNormalizer#resolve(String, String, String)}) before any other rule runs. The
 * structured form ({@code addressLine1}/{@code addressLine2}) is preferred when supplied, otherwise
 * the flat {@code address} is used; either way the lines and the composed flat address are
 * normalized. Running first means the required-field check ({@link ValidateOwnerFields}) rejects an
 * address that is blank after normalization and {@link BuildOwner} stores — and later responses
 * return — the normalized form. Binds the request body once and republishes it for the rest of the
 * pipeline.
 */
public class NormalizeAddress {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> normalized) {
        AddressNormalizer.Normalized address = AddressNormalizer.resolve(
                request.getAddressLine1(), request.getAddressLine2(), request.getAddress());
        request.setAddressLine1(address.addressLine1());
        request.setAddressLine2(address.addressLine2());
        request.setAddress(address.address());
        normalized.set(request);
    }
}
