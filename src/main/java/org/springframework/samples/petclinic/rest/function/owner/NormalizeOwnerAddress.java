package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Runs early in the create-owner pipeline (before {@link RequireOwnerFields}): normalizes the
 * {@code address} to its canonical form (see {@link AddressNormalizer}). The normalized value is
 * written back onto the (shared) {@link OwnerFieldsDto} so {@link RequireOwnerFields} rejects an
 * address that is blank after normalization, {@link BuildOwner} stores it and {@code GET /owners/{id}}
 * later returns it.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto owner) {
        owner.setAddress(AddressNormalizer.normalize(owner.getAddress()));
    }
}
