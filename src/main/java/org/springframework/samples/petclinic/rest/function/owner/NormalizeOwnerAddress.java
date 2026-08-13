package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}: reads the request body once and normalizes the address in
 * place (trim and collapse whitespace, upper-case, expand common abbreviations — see
 * {@link OwnerAddressNormalizer}), then republishes the body for the rest of the pipeline. Running
 * before {@link RequireOwnerFields} means the required-field check sees the normalized address, so
 * an address that is blank after normalization is rejected; it also means the stored and returned
 * address, and every later address comparison, use the normalized form.
 */
public class NormalizeOwnerAddress {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated) {
        request.setAddress(OwnerAddressNormalizer.normalize(request.getAddress()));
        validated.set(request);
    }
}
