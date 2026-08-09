package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes the owner address when creating an owner and writes the canonical form back onto the
 * request, so that {@link BuildOwner} persists — and the response returns — the normalized value.
 * Normalization is defined by {@link AddressNormalizer}: trim, collapse whitespace, upper-case and
 * expand common street-type abbreviations.
 *
 * <p>Runs after {@link ValidateOwner} has rejected a blank (after normalization) address, so the
 * value here is always non-blank; the downstream household checks then compare the normalized form.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) {
        request.setAddress(AddressNormalizer.normalize(request.getAddress()));
    }
}
