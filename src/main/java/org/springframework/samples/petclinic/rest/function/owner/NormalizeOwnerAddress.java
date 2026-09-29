package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.BlankAddressException;

/**
 * Normalizes the address on create: trims and collapses whitespace, upper-cases, and
 * expands the common street-type abbreviations (see {@link AddressNormalizer}). The
 * normalized value is stored back on the request so later steps build, compare, save and
 * return it. The required-field check runs against the normalized form: an address that is
 * blank after normalization is rejected 400 via {@link BlankAddressException}.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) throws BlankAddressException {
        String raw = request.getAddress();
        String normalized = AddressNormalizer.normalize(raw);
        if (normalized.isEmpty()) {
            throw new BlankAddressException(raw);
        }
        request.setAddress(normalized);
    }
}
