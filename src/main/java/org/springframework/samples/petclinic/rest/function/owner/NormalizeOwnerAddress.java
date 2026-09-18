package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes an owner request's address in place into canonical form (see {@link AddressNormalizer}):
 * trimmed, whitespace-collapsed, upper-cased, with common street-type abbreviations expanded. Runs
 * after the field is known non-blank and before the DTO is applied to an entity, so owner addresses
 * are stored, returned and household-checked as their normalized value.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) {
        request.setAddress(AddressNormalizer.normalize(request.getAddress()));
    }
}
