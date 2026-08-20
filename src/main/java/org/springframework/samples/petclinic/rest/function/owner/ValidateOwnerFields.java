package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}. Rejects a body that is missing or blank in any
 * of firstName, lastName, address, city or telephone with a 400 (listing every offending
 * field) before the owner is built, then publishes the body for later steps.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        List<String> missing = new ArrayList<>();
        if (isBlank(request.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            missing.add("lastName");
        }
        // Address may be supplied in EITHER form: the structured 'addressLine1' or the flat
        // 'address'. It is checked against its normalized form, so an entry that collapses to
        // blank once whitespace is trimmed counts as absent. Missing in both forms is a 400.
        if (AddressNormalizer.normalize(request.getAddressLine1()).isEmpty()
                && AddressNormalizer.normalize(request.getAddress()).isEmpty()) {
            missing.add("address");
        }
        if (isBlank(request.getCity())) {
            missing.add("city");
        }
        if (isBlank(request.getTelephone())) {
            missing.add("telephone");
        }
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
