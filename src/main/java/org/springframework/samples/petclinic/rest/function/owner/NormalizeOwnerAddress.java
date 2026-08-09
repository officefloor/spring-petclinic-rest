package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes the owner address when creating an owner and writes the canonical form back onto the
 * request, so that {@link BuildOwner} persists — and the response returns — the normalized value.
 * Normalization is defined by {@link AddressNormalizer}: trim, collapse whitespace, upper-case and
 * expand common street-type abbreviations.
 *
 * <p>The address may be supplied in either form. When the structured {@code addressLine1} is present
 * it is preferred: {@code addressLine1} and the optional {@code addressLine2} are normalized, and the
 * flat {@code address} is (re)composed as the normalized {@code addressLine1} with a single space and
 * the normalized {@code addressLine2} appended when an {@code addressLine2} is present. Otherwise only
 * the flat {@code address} is normalized. Downstream steps and the response therefore read the
 * structured value when supplied, falling back to the flat address.
 *
 * <p>Runs after {@link ValidateOwner} has rejected a request with no address in either form, so the
 * chosen value here is always non-blank; the downstream household checks then compare the composed form.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) {
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        if (!line1.isBlank()) {
            // Structured form present — prefer it and compose the flat address from it.
            String line2 = AddressNormalizer.normalize(request.getAddressLine2());
            request.setAddressLine1(line1);
            request.setAddressLine2(line2.isBlank() ? null : line2);
            request.setAddress(line2.isBlank() ? line1 : line1 + " " + line2);
        }
        else {
            // Flat form only — normalize the address as before.
            request.setAddress(AddressNormalizer.normalize(request.getAddress()));
        }
    }
}
