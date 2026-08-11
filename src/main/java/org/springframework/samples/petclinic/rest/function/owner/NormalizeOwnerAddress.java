package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Step of {@code POST /api/owners}: normalises the owner's address to its canonical form (trim and
 * collapse whitespace, upper-case, expand common abbreviations — see {@link AddressNormalizer}).
 *
 * <p>The structured form is preferred when present: when {@code addressLine1} is supplied it is
 * normalised (and {@code addressLine2}, when present), and the flat {@code address} is composed from
 * them — the normalized {@code addressLine1}, with a single space and the normalized
 * {@code addressLine2} appended when {@code addressLine2} is present. When only the flat
 * {@code address} is supplied it is normalised in place, keeping the request contract
 * backward-compatible. The canonical values are written back onto the (shared) request DTO so they
 * are stored and returned.
 *
 * <p>{@link RequireOwnerFields} has already rejected 400 an owner that supplies neither a non-blank
 * {@code addressLine1} nor a flat {@code address}, so by the time this runs an address is present in
 * one form or the other.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) {
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        if (!line1.isEmpty()) {
            // Structured form: prefer the address lines and compose the flat address from them.
            String line2 = AddressNormalizer.normalize(request.getAddressLine2());
            request.setAddressLine1(line1);
            request.setAddressLine2(line2.isEmpty() ? null : line2);
            request.setAddress(line2.isEmpty() ? line1 : line1 + " " + line2);
        }
        else {
            // Flat form (backward-compatible): normalise the free-text address in place.
            request.setAddress(AddressNormalizer.normalize(request.getAddress()));
        }
    }
}
