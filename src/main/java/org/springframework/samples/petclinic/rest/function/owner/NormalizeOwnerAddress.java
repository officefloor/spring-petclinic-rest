package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes a create request's address to its canonical form — trimmed, whitespace
 * collapsed, upper-cased and with common street-type abbreviations expanded (see
 * {@link Addresses#normalize(String)}) — storing the result back on the body so later
 * steps compare, persist and return the canonical value. Normalization applies to
 * whichever address fields are supplied: the structured lines are normalized in place and
 * the flat {@code address} is set to the composed canonical form (see
 * {@link Addresses#canonical}), preferring the structured lines when present and falling
 * back to the flat address otherwise. Runs after {@link ValidateOwnerFields} has confirmed
 * an address is present in one of the two forms and published the body; mutates that same
 * instance in place.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) {
        request.setAddressLine1(Addresses.normalize(request.getAddressLine1()));
        request.setAddressLine2(Addresses.normalize(request.getAddressLine2()));
        request.setAddress(Addresses.canonical(request.getAddressLine1(),
                request.getAddressLine2(), request.getAddress()));
    }
}
