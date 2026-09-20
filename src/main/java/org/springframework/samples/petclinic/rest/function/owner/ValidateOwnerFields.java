package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create request that is missing or blank in any required field
 * (firstName, lastName, address, city, telephone) before {@link BuildOwner} maps the
 * body, reporting every offending field at once. An address may be supplied in either
 * form — a non-blank structured {@code addressLine1} or the flat {@code address} — and
 * the request is accepted when at least one is present, keeping earlier minimal owners
 * backward-compatible. Publishes the body for later steps so the request is bound only
 * here.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        List<String> missing = new ArrayList<>();
        requireText(missing, "firstName", request.getFirstName());
        requireText(missing, "lastName", request.getLastName());
        // Accept an address in either form; the canonical value is blank only when neither
        // the structured addressLine1 nor the flat address supplies one (see Addresses).
        requireText(missing, "address", Addresses.canonical(request.getAddressLine1(),
                request.getAddressLine2(), request.getAddress()));
        requireText(missing, "city", request.getCity());
        requireText(missing, "telephone", request.getTelephone());
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        validated.set(request);
    }

    private static void requireText(List<String> missing, String field, String value) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }
}
