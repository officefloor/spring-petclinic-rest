package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Step of {@code POST /api/owners}: normalises the owner {@code address} to its canonical form
 * (trim and collapse whitespace, upper-case, expand common abbreviations — see
 * {@link AddressNormalizer}). The canonical value is written back onto the (shared) request DTO so
 * it is stored and returned as {@code address}. Blank addresses are already rejected 400 upstream by
 * {@link RequireOwnerFields}, so by the time this runs the address is non-blank.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) {
        request.setAddress(AddressNormalizer.normalize(request.getAddress()));
    }
}
