package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of create-owner: reads the request body once, normalizes whichever address fields are
 * supplied into their canonical form (see {@link AddressNormalizer}) in place, and republishes the
 * body for later steps, so no later step binds {@code @RequestBody} again.
 *
 * <p>The structured fields are preferred: when a non-blank {@code addressLine1} is present the flat
 * {@code address} is (re)composed from the normalized lines — the normalized {@code addressLine1},
 * with a single space and the normalized {@code addressLine2} appended when {@code addressLine2} is
 * present. When only the flat {@code address} is supplied it is normalized as before, keeping the
 * request backward-compatible. Running before {@link ValidateOwnerFields} means the required-field
 * check sees the composed address and rejects a request that carries no address in either form, and
 * every downstream comparison works from the stored, normalized value.
 */
public class NormalizeOwnerAddress {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> body) {
        boolean hasLine1 = isPresent(request.getAddressLine1());
        boolean hasLine2 = isPresent(request.getAddressLine2());
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        String line2 = AddressNormalizer.normalize(request.getAddressLine2());
        String flat = AddressNormalizer.normalize(request.getAddress());

        request.setAddressLine1(hasLine1 ? line1 : null);
        request.setAddressLine2(hasLine2 ? line2 : null);

        String composed;
        if (hasLine1) {
            composed = hasLine2 ? line1 + " " + line2 : line1;
        }
        else {
            composed = flat;
        }
        request.setAddress(composed);
        body.set(request);
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
