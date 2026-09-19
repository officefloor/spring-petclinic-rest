package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes the address on a create request to its canonical form (see
 * {@link AddressNormalizer}). Normalization applies to whichever address fields are supplied:
 * each structured field ({@code addressLine1}, {@code addressLine2}) is normalized in place, and
 * the flat {@code address} is set to the canonical value — the composed structured address when a
 * structured {@code addressLine1} is present, otherwise the normalized flat address. Storing it
 * back on the shared {@code @Val} request means duplicate detection, {@link BuildOwner} and the
 * response all read the same canonical form. Runs after {@link ValidateNewOwner} has confirmed an
 * address is present, and before {@link EnsureUniqueIdentity} so the derived identity key uses it.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) {
        if (AddressNormalizer.isPresent(request.getAddressLine1())) {
            request.setAddressLine1(AddressNormalizer.normalize(request.getAddressLine1()));
        }
        if (AddressNormalizer.isPresent(request.getAddressLine2())) {
            request.setAddressLine2(AddressNormalizer.normalize(request.getAddressLine2()));
        }
        request.setAddress(AddressNormalizer.canonical(
                request.getAddressLine1(), request.getAddressLine2(), request.getAddress()));
    }
}
