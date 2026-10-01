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
 * <p>The telephone is converted to E.164 form (see {@link OwnerTelephone#toE164}). The E.164 value
 * replaces the submitted one on the republished body, so it is what gets stored and returned. A
 * number that cannot form valid E.164 is rejected as an invalid {@code telephone}.
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
            String telephone = OwnerTelephone.toE164(request.getTelephone());
            if (telephone == null) {
                errors.add("telephone");
            }
            else {
                request.setTelephone(telephone);
            }
        }
        // Email is optional. When present it must be syntactically valid; it is normalized to
        // lower case so it is stored and returned lower-cased.
        String email = request.getEmail();
        if (email != null) {
            if (!OwnerEmail.isValid(email)) {
                errors.add("email");
            }
            else {
                request.setEmail(OwnerEmail.normalize(email));
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
