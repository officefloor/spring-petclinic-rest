package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-owner step: rewrites the address into canonical form (see
 * {@link AddressNormalizer}) so the stored and returned address is trimmed, collapsed,
 * upper-cased and has common abbreviations expanded. Normalization applies to whichever
 * structured fields ({@code addressLine1}, {@code addressLine2}) were supplied, and the flat
 * {@code address} is (re)written to the composed canonical string: the normalized first line,
 * preferring the structured form, with the normalized second line appended when present.
 *
 * <p>Runs after {@link ValidateOwnerFields} (which guarantees an address was supplied in one
 * form or the other) and before {@link BuildOwner}, mutating the published body in place.
 * Mirrors {@link NormalizeOwnerTelephone} and {@link NormalizeOwnerEmail}.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) {
        String line1 = AddressNormalizer.line1(request.getAddressLine1(), request.getAddress());
        String composed = AddressNormalizer.compose(line1, request.getAddressLine2());
        if (!AddressNormalizer.isBlank(request.getAddressLine1())) {
            request.setAddressLine1(AddressNormalizer.normalize(request.getAddressLine1()));
        }
        if (!AddressNormalizer.isBlank(request.getAddressLine2())) {
            request.setAddressLine2(AddressNormalizer.normalize(request.getAddressLine2()));
        }
        request.setAddress(composed);
    }
}
