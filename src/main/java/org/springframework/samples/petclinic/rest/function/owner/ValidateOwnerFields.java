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
 * It then normalizes the telephone by stripping every non-digit character and requires
 * exactly 10 digits, rejecting with an {@link InvalidTelephoneException} (400) otherwise.
 * On success it stores the normalized 10-digit telephone and republishes the body for
 * {@link BuildOwner}.
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
        String telephone = request.getTelephone().replaceAll("\\D", "");
        if (telephone.length() != 10) {
            throw new InvalidTelephoneException(
                    "Telephone must be exactly 10 digits after removing non-digit characters");
        }
        request.setTelephone(telephone);
        request.setEmail(OwnerEmail.normalize(request.getEmail()));
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
