package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes a create request's address to its canonical form — trimmed, whitespace
 * collapsed, upper-cased and with common street-type abbreviations expanded (see
 * {@link Addresses#normalize(String)}) — storing the result back on the body so later
 * steps compare, persist and return the canonical value. Runs after
 * {@link ValidateOwnerFields} has confirmed the address is not blank once normalized and
 * published the body; mutates that same instance in place.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) {
        request.setAddress(Addresses.normalize(request.getAddress()));
    }
}
