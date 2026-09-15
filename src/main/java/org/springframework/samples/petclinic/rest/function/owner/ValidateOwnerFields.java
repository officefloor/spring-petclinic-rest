package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create request whose required owner fields are missing or blank. Runs
 * first in the {@code POST /api/owners} pipeline so an incomplete body is a 400
 * before any owner is built or saved. It binds the body once and republishes it as
 * a variable for the later {@link BuildOwner} step.
 *
 * <p>The address is normalized in place first (see {@link AddressNormalizer}) so the
 * required-field check rejects an address that is blank once normalized, and every
 * later step — including the household comparisons and the stored, returned value —
 * sees the canonical form.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        request.setAddress(AddressNormalizer.normalize(request.getAddress()));
        List<String> missing = new ArrayList<>();
        require(missing, "firstName", request.getFirstName());
        require(missing, "lastName", request.getLastName());
        require(missing, "address", request.getAddress());
        require(missing, "city", request.getCity());
        require(missing, "telephone", request.getTelephone());
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        validated.set(request);
    }

    private static void require(List<String> missing, String field, String value) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }
}
