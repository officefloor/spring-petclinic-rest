package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Runs early in the create-owner pipeline (before {@link RequireOwnerFields}): normalizes whichever
 * address fields the request supplied to their canonical form (see {@link AddressNormalizer}).
 *
 * <p>The request may supply a structured address ({@code addressLine1}, optional {@code addressLine2})
 * or the flat {@code address}, kept for backward compatibility. Structured fields are preferred: when
 * {@code addressLine1} is present (non-blank), each supplied structured line is normalized in place and
 * the flat {@code address} is set to the composed value — the normalized {@code addressLine1}, with a
 * single space and the normalized {@code addressLine2} appended when {@code addressLine2} is present
 * (see {@link AddressNormalizer#compose(String, String)}). Otherwise the flat {@code address} is
 * normalized on its own.
 *
 * <p>The result is written back onto the (shared) {@link OwnerFieldsDto} so {@link RequireOwnerFields}
 * rejects an owner supplying neither form, {@link BuildOwner} stores the normalized/composed values and
 * {@code GET /owners/{id}} later returns them.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto owner) {
        String line1 = owner.getAddressLine1();
        if (AddressNormalizer.hasStructuredAddress(line1)) {
            String normalizedLine1 = AddressNormalizer.normalize(line1);
            owner.setAddressLine1(normalizedLine1);
            String line2 = owner.getAddressLine2();
            String normalizedLine2 = line2 != null && !line2.isBlank()
                    ? AddressNormalizer.normalize(line2) : null;
            owner.setAddressLine2(normalizedLine2);
            owner.setAddress(AddressNormalizer.compose(normalizedLine1, normalizedLine2));
        }
        else {
            owner.setAddress(AddressNormalizer.normalize(owner.getAddress()));
        }
    }
}
