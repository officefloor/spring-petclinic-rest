package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create-owner request that is missing or blank in any required field
 * ({@code firstName}, {@code lastName}, {@code address}, {@code city}, {@code telephone}).
 *
 * <p>Runs first in the {@code POST /api/owners} pipeline and binds the body without
 * {@code @Valid}, so a missing field surfaces as a {@link MissingOwnerFieldsException}
 * (400 with an {@code errors} array) rather than the generic schema-validation response.
 * It then normalizes the telephone to E.164 form (see {@link TelephoneE164}), rejecting
 * with an {@link InvalidTelephoneException} (400) when it cannot form a valid E.164 number.
 * On success it stores the E.164 telephone and republishes the body for {@link BuildOwner}.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidTelephoneException, InvalidEmailException {
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
        if (!errors.isEmpty()) {
            throw new MissingOwnerFieldsException(errors);
        }
        String telephone = TelephoneE164.normalize(request.getTelephone());
        if (telephone == null) {
            throw new InvalidTelephoneException(
                    "Telephone cannot be normalized to a valid E.164 number");
        }
        request.setTelephone(telephone);
        request.setEmail(OwnerEmail.normalize(request.getEmail()));
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
