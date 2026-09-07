package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.MissingFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}. Rejects a request that is missing or blank in any
 * required owner field before {@link BuildOwner} runs, throwing {@link MissingFieldsException}
 * (handled as a 400 listing the offending field names). Normalizes the telephone by stripping
 * every non-digit character and requires exactly ten digits, throwing
 * {@link InvalidTelephoneException} (400) otherwise. Publishes the normalized body for later steps.
 */
public class RequireOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingFieldsException, InvalidTelephoneException, InvalidEmailException {
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
            throw new MissingFieldsException(missing);
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
