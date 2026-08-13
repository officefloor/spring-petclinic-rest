package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}: reads the request body once and normalizes whichever
 * address form was supplied, then republishes the body for the rest of the pipeline.
 *
 * <p>The structured form (a non-blank {@code addressLine1}, with an optional {@code addressLine2})
 * is preferred when present; otherwise the flat {@code address} input is used (backward compatible).
 * Every supplied line is normalized in place (trim and collapse whitespace, upper-case, expand
 * common abbreviations — see {@link OwnerAddressNormalizer}). The flat {@code address} is set to the
 * composed, normalized value: the normalized {@code addressLine1}, with a single space and the
 * normalized {@code addressLine2} appended when {@code addressLine2} is present. Running before
 * {@link RequireOwnerFields} means the required-field check sees the composed address, so a request
 * that supplies no address in either form is rejected; it also means the stored and returned
 * address, and every later step that reads the address, use the normalized form.
 */
public class NormalizeOwnerAddress {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated) {
        String line1 = request.getAddressLine1();
        if (line1 != null && !line1.isBlank()) {
            // Structured form takes precedence over the flat 'address'.
            String normalizedLine1 = OwnerAddressNormalizer.normalize(line1);
            String normalizedLine2 = OwnerAddressNormalizer.normalize(request.getAddressLine2());
            request.setAddressLine1(normalizedLine1);
            request.setAddressLine2(normalizedLine2.isEmpty() ? null : normalizedLine2);
            String composed = normalizedLine2.isEmpty() ? normalizedLine1
                    : normalizedLine1 + " " + normalizedLine2;
            request.setAddress(composed);
        }
        else {
            request.setAddress(OwnerAddressNormalizer.normalize(request.getAddress()));
        }
        validated.set(request);
    }
}
