package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerFieldsInvalidException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}: rejects a request that is missing or blank in any of
 * firstName, lastName, address, city or telephone. Throwing lists every offending field, so the
 * client sees them all at once. It also normalizes the telephone by stripping every non-digit
 * character and requiring exactly 10 digits, rejecting with 400 otherwise; the normalized value is
 * written back onto the body. On success it publishes the body for {@link BuildOwner} to map.
 */
public class ValidateOwnerFields {

    /** Owner telephone must be exactly this many digits after non-digits are stripped. */
    private static final int TELEPHONE_DIGITS = 10;

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsInvalidException {
        List<String> errors = new ArrayList<>();
        checkPresent("firstName", request.getFirstName(), errors);
        checkPresent("lastName", request.getLastName(), errors);
        checkPresent("address", request.getAddress(), errors);
        checkPresent("city", request.getCity(), errors);
        checkPresent("telephone", request.getTelephone(), errors);
        if (!errors.isEmpty()) {
            throw new OwnerFieldsInvalidException(errors);
        }
        String telephone = request.getTelephone().replaceAll("\\D", "");
        if (telephone.length() != TELEPHONE_DIGITS) {
            throw new OwnerFieldsInvalidException(List.of("telephone"));
        }
        request.setTelephone(telephone);
        validated.set(request);
    }

    private static void checkPresent(String field, String value, List<String> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.add(field);
        }
    }
}
