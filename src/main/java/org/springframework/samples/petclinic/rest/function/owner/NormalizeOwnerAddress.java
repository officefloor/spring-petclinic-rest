package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidAddressException;

/**
 * Normalizes the submitted address on create via {@link OwnerAddresses} (trim and collapse
 * whitespace, upper-case, expand common abbreviations). The normalized value is written
 * back onto the request in place (the {@code @Val} object is shared with later steps) so
 * downstream steps store, compare and return it. An address that is blank after
 * normalization is rejected with 400 via {@link InvalidAddressException}.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) throws InvalidAddressException {
        String address = request.getAddress();
        String normalized = OwnerAddresses.normalize(address);
        if (normalized == null || normalized.isEmpty()) {
            throw new InvalidAddressException(address);
        }
        request.setAddress(normalized);
    }
}
