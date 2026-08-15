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
 * First step of {@code POST /api/owners}. Rejects a request that is missing or blank in any
 * required field (firstName, lastName, address, city, telephone) with a 400 whose body lists
 * the offending field names. Then normalizes the telephone by stripping every non-digit
 * character and requires exactly 10 digits, storing the 10-digit value back on the request so
 * later steps persist and return it; a telephone that is not exactly 10 digits after stripping
 * is rejected with a 400. Runs before {@link BuildOwner} maps the body to an {@link
 * org.springframework.samples.petclinic.model.Owner}.
 */
public class ValidateNewOwner {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidTelephoneException, InvalidEmailException {
        List<String> missing = new ArrayList<>();
        if (isBlank(request.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            missing.add("lastName");
        }
        if (isBlank(request.getAddress())) {
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
        String normalized = request.getTelephone().replaceAll("\\D", "");
        if (normalized.length() != 10) {
            throw new InvalidTelephoneException(request.getTelephone());
        }
        request.setTelephone(normalized);
        request.setEmail(OwnerEmail.normalize(request.getEmail()));
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
