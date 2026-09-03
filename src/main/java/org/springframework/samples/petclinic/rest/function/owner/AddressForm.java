package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.AddressRequiredException;

/**
 * Resolves the request's address into its normalized, stored form. The structured fields
 * are preferred: when {@code addressLine1} is non-blank the stored {@code address} becomes
 * the normalized line 1 with the normalized {@code addressLine2} appended after a single
 * space (when present). Otherwise the flat {@code address} is normalized as before, which
 * rejects a request that supplies neither form.
 */
final class AddressForm {

    private AddressForm() {
    }

    static void apply(OwnerFieldsDto request) throws AddressRequiredException {
        String line1 = request.getAddressLine1();
        if (line1 == null || line1.isBlank()) {
            request.setAddress(Address.normalize(request.getAddress()));
            return;
        }
        String normalizedLine1 = Address.normalize(line1);
        String line2 = request.getAddressLine2();
        boolean hasLine2 = line2 != null && !line2.isBlank();
        String normalizedLine2 = hasLine2 ? Address.normalize(line2) : null;
        request.setAddressLine1(normalizedLine1);
        request.setAddressLine2(normalizedLine2);
        request.setAddress(hasLine2 ? normalizedLine1 + " " + normalizedLine2 : normalizedLine1);
    }
}
