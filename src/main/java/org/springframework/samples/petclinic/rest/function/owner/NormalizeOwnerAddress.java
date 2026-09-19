package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes the address on a create request to its canonical form (see
 * {@link AddressNormalizer}). Stores the normalized value back on the shared {@code @Val}
 * request so duplicate detection, {@link BuildOwner} and the response all carry it. Runs
 * after {@link ValidateNewOwner} has confirmed the field is non-blank after normalization,
 * and before {@link EnsureUniqueIdentity} so the derived identity key uses the normalized form.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) {
        request.setAddress(AddressNormalizer.normalize(request.getAddress()));
    }
}
