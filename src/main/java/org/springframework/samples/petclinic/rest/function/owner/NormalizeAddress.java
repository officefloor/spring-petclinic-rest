package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Normalizes a create-owner request's address to its canonical form (see {@link AddressNormalizer}:
 * trim, collapse whitespace, upper-case, expand common abbreviations) before any other step runs. Binds
 * the request body (the single {@code @RequestBody} step of this pipeline) and republishes it as a
 * variable for the later steps.
 *
 * <p>The address may arrive in either form. The structured fields are preferred when present: a
 * non-blank {@code addressLine1} (with an optional {@code addressLine2}) is normalized line-by-line and
 * composed into the flat {@code address} — the normalized {@code addressLine1}, with a single space and
 * the normalized {@code addressLine2} appended when an {@code addressLine2} is present. Otherwise the
 * flat {@code address} input is normalized as before, keeping the create contract backward-compatible.
 * Either way the normalized {@code address} is what {@link RequireOwnerFields} tests for blankness, what
 * everything reading the address derives from, and what {@link BuildOwner} maps to the entity — so it is
 * stored and returned, alongside the normalized structured fields.
 */
public class NormalizeAddress {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> normalized) {
        String line1 = request.getAddressLine1();
        if (line1 != null && !line1.isBlank()) {
            String normalizedLine1 = AddressNormalizer.normalize(line1);
            String normalizedLine2 = AddressNormalizer.normalize(request.getAddressLine2());
            request.setAddressLine1(normalizedLine1);
            request.setAddressLine2(normalizedLine2.isEmpty() ? null : normalizedLine2);
            request.setAddress(normalizedLine2.isEmpty() ? normalizedLine1
                    : normalizedLine1 + " " + normalizedLine2);
        }
        else {
            request.setAddress(AddressNormalizer.normalize(request.getAddress()));
        }
        normalized.set(request);
    }
}
