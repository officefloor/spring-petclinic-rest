package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingRequiredFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Runs first in the create-owner pipeline. Rejects a request that is missing or blank in any of
 * firstName, lastName, address, city or telephone by throwing {@link MissingRequiredFieldsException}
 * (handled as 400). Binds the body (without {@code @Valid}) so a blank/missing field is reported as a
 * required-field error rather than a bean-validation error, then republishes it for {@link BuildOwner}.
 *
 * <p>An address may be supplied in either form: the structured {@code addressLine1} (with an optional
 * {@code addressLine2}) is preferred, and the flat {@code address} remains accepted for backward
 * compatibility. The request satisfies the address requirement when it supplies a non-blank
 * {@code addressLine1} or a non-blank flat {@code address}; supplying neither is the sole "address"
 * required-field error.
 *
 * <p>The address is {@link AddressNormalizer normalized} (trimmed, whitespace-collapsed, upper-cased,
 * abbreviations expanded) before it is checked, so an address that is blank <em>after</em> normalization
 * is rejected. The normalized structured lines and the {@link AddressNormalizer#compose composed}
 * address are written back so downstream steps store, return and compare them in that one canonical form.
 */
public class ValidateRequiredOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingRequiredFieldsException {
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        String line2 = AddressNormalizer.normalize(request.getAddressLine2());
        boolean structured = !line1.isEmpty();
        String address = AddressNormalizer.compose(request.getAddressLine1(), request.getAddressLine2(),
                request.getAddress());
        List<String> missing = new ArrayList<>();
        addIfBlank(missing, "firstName", request.getFirstName());
        addIfBlank(missing, "lastName", request.getLastName());
        addIfBlank(missing, "address", address);
        addIfBlank(missing, "city", request.getCity());
        addIfBlank(missing, "telephone", request.getTelephone());
        if (!missing.isEmpty()) {
            throw new MissingRequiredFieldsException(missing);
        }
        if (structured) {
            request.setAddressLine1(line1);
            request.setAddressLine2(line2.isEmpty() ? null : line2);
        }
        request.setAddress(address);
        validated.set(request);
    }

    private static void addIfBlank(List<String> missing, String field, String value) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }
}
