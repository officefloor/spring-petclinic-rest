package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes an owner's {@code address} to its canonical form (trimmed, whitespace collapsed,
 * upper-cased, abbreviations expanded) in place on the shared body, so the persisted and
 * returned address is that canonical string and every later address comparison sees it. It
 * runs before validation, so an address that is blank once normalized is rejected there as a
 * missing required field. It mutates the already-published body variable rather than binding
 * {@code @RequestBody}, so it composes after the sole binding step in the pipeline.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) {
        String address = request.getAddress();
        if (address == null) {
            return; // absence is reported by the required-field check
        }
        request.setAddress(OwnerAddress.normalize(address));
    }
}
