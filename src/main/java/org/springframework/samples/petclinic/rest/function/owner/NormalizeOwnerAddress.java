package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-owner step: rewrites the address into canonical form (see
 * {@link AddressNormalizer}) so the stored and returned address is trimmed, collapsed,
 * upper-cased and has common abbreviations expanded. Runs after {@link ValidateOwnerFields}
 * (which guarantees the value is non-blank once normalized) and before {@link BuildOwner},
 * mutating the published body in place. Mirrors {@link NormalizeOwnerTelephone} and
 * {@link NormalizeOwnerEmail}.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) {
        request.setAddress(AddressNormalizer.normalize(request.getAddress()));
    }
}
