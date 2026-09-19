package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.AddressNormalizer;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of the create-owner pipeline: reads the request body once, normalizes its
 * address to canonical form (see {@link AddressNormalizer}) and republishes the body so
 * downstream steps consume it as {@code @Val}. Running before {@link ValidateOwnerFields}
 * means the required-field check sees the normalized address, so an address that is blank
 * only after normalization is rejected; and because {@link BuildOwner} stores the same
 * value, the persisted and returned address is the normalized one. Household comparison
 * (see {@link Households}) then works from the normalized form too.
 */
public class NormalizeOwnerAddress {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> normalized) {
        request.setAddress(AddressNormalizer.normalize(request.getAddress()));
        normalized.set(request);
    }
}
