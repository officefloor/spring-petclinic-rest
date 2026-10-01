package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create request that is missing or blank in any required owner field.
 *
 * <p>Runs first in the {@code POST /api/owners} pipeline. It binds the body once (this is the only
 * step that reads {@code @RequestBody}) and republishes it for {@link BuildOwner}. Each of
 * firstName, lastName, address, city and telephone must be present and non-blank; any that are not
 * are collected and reported together via {@link OwnerFieldsInvalidException}.
 *
 * <p>The telephone is normalized by stripping every non-digit character; it must contain exactly
 * ten digits afterwards. The 10-digit value replaces the submitted one on the republished body, so
 * it is what gets stored and returned. Anything else is rejected as an invalid {@code telephone}.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsInvalidException {
        List<String> errors = new ArrayList<>();
        if (isBlank(request.getFirstName())) {
            errors.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            errors.add("lastName");
        }
        if (isBlank(request.getAddress())) {
            errors.add("address");
        }
        if (isBlank(request.getCity())) {
            errors.add("city");
        }
        if (isBlank(request.getTelephone())) {
            errors.add("telephone");
        }
        else {
            String telephone = request.getTelephone().replaceAll("\\D", "");
            if (telephone.length() != 10) {
                errors.add("telephone");
            }
            else {
                request.setTelephone(telephone);
            }
        }
        if (!errors.isEmpty()) {
            throw new OwnerFieldsInvalidException(errors);
        }
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
