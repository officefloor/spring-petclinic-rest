package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes the create-owner address: trims and collapses whitespace, upper-cases and expands
 * common street-type abbreviations ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}).
 * Mutates the published request in place so later steps store and return the normalized value, and
 * so household duplicate detection and the shared household id compare the normalized form.
 *
 * <p>{@link ValidateOwnerFields} has already rejected an address that is blank after normalization,
 * so at this point the value normalizes to a non-empty string.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) {
        request.setAddress(AddressNormalizer.normalize(request.getAddress()));
    }
}
