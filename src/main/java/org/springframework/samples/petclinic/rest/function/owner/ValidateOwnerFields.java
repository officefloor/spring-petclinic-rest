package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}. Rejects a create request that is missing or blank in any
 * required field, so an invalid body is a 400 whose {@code errors} array names each offending field.
 * Runs before {@link BuildOwner}, which then reads the published body via {@code @Val}.
 *
 * <p>The address is normalized in place first (see {@link AddressNormalizer}) so it is stored and
 * returned in normalized form, and so the required-field check rejects an address that is blank after
 * normalization (for example one made only of whitespace). An owner may supply its address in EITHER
 * form: the structured {@code addressLine1} (with an optional {@code addressLine2}), which is preferred
 * when present, or the flat {@code address} that stays accepted for backward compatibility. When the
 * structured form is present the flat {@code address} is (re)composed as the normalized
 * {@code addressLine1}, with a single space and the normalized {@code addressLine2} appended when that
 * second line is present, so everything downstream reading {@code address} sees the structured value.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        // Normalize whichever address fields are supplied.
        if (request.getAddressLine1() != null) {
            request.setAddressLine1(AddressNormalizer.normalize(request.getAddressLine1()));
        }
        if (request.getAddressLine2() != null) {
            request.setAddressLine2(AddressNormalizer.normalize(request.getAddressLine2()));
        }
        if (request.getAddress() != null) {
            request.setAddress(AddressNormalizer.normalize(request.getAddress()));
        }
        // Prefer the structured fields: compose the flat address from them when addressLine1 is present.
        String line1 = request.getAddressLine1();
        if (line1 != null && !line1.isBlank()) {
            String line2 = request.getAddressLine2();
            request.setAddress(line2 != null && !line2.isBlank() ? line1 + " " + line2 : line1);
        }
        List<String> errors = new ArrayList<>();
        require(errors, "firstName", request.getFirstName());
        require(errors, "lastName", request.getLastName());
        require(errors, "address", request.getAddress());
        require(errors, "city", request.getCity());
        require(errors, "telephone", request.getTelephone());
        if (!errors.isEmpty()) {
            throw new MissingOwnerFieldsException(errors);
        }
        validated.set(request);
    }

    private static void require(List<String> errors, String field, String value) {
        if (value == null || value.isBlank()) {
            errors.add(field);
        }
    }
}
