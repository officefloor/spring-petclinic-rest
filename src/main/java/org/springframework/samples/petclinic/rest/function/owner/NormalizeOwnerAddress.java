package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes an owner's address to its canonical form (trimmed, whitespace collapsed, upper-cased,
 * abbreviations expanded) in place on the shared body, so the persisted and returned address is
 * that canonical string and every later address comparison sees it. Normalization applies to
 * whichever address fields the request supplied: the structured {@code addressLine1}/
 * {@code addressLine2} and the flat {@code address}.
 *
 * <p>The structured form is preferred: when {@code addressLine1} is present the {@code address} is
 * set to the {@link OwnerAddress#compose composed} lines, so the flat {@code address} always holds
 * the effective, normalized address; otherwise the supplied flat {@code address} is normalized in
 * place. It runs before validation, so an owner that supplies neither form is rejected there as a
 * missing required field. It mutates the already-published body variable rather than binding
 * {@code @RequestBody}, so it composes after the sole binding step in the pipeline.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) {
        // Normalize each supplied structured line in place, leaving an absent line absent.
        if (request.getAddressLine1() != null) {
            request.setAddressLine1(OwnerAddress.normalize(request.getAddressLine1()));
        }
        if (request.getAddressLine2() != null) {
            request.setAddressLine2(OwnerAddress.normalize(request.getAddressLine2()));
        }

        String addressLine1 = request.getAddressLine1();
        if (addressLine1 != null && !addressLine1.isBlank()) {
            // Structured form present: the effective address is the composed lines.
            request.setAddress(OwnerAddress.compose(addressLine1, request.getAddressLine2()));
        }
        else if (request.getAddress() != null) {
            // Flat form only: normalize it in place (absence is reported by validation).
            request.setAddress(OwnerAddress.normalize(request.getAddress()));
        }
    }
}
