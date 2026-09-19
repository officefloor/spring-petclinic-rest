package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.AddressNormalizer;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of the create-owner pipeline: reads the request body once, normalizes whichever
 * address fields it supplies to canonical form (see {@link AddressNormalizer}) and republishes
 * the body so downstream steps consume it as {@code @Val}.
 *
 * <p>The structured fields are preferred when present: a non-blank {@code addressLine1} (with an
 * optional {@code addressLine2}) is normalized in place, and the flat {@code address} is set to
 * their composed form. Otherwise the flat {@code address} is normalized as before, keeping the
 * contract backward-compatible. Either way the flat {@code address} carries the effective
 * address, so everything downstream — {@link ValidateOwnerFields}, {@link BuildOwner} (hence the
 * persisted and returned value) and household comparison (see {@link Households}) — works from the
 * normalized, preferred form.
 */
public class NormalizeOwnerAddress {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> normalized) {
        String line1 = request.getAddressLine1();
        if (line1 != null && !line1.isBlank()) {
            String normalizedLine2 = AddressNormalizer.normalize(request.getAddressLine2());
            request.setAddressLine1(AddressNormalizer.normalize(line1));
            request.setAddressLine2(normalizedLine2.isEmpty() ? null : normalizedLine2);
            request.setAddress(AddressNormalizer.compose(line1, normalizedLine2));
        }
        else {
            request.setAddress(AddressNormalizer.normalize(request.getAddress()));
        }
        normalized.set(request);
    }
}
