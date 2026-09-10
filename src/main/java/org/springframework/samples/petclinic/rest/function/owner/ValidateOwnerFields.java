package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create-owner request that is missing or blank in any of firstName, lastName,
 * address, city or telephone, before {@link BuildOwner} runs. Runs first so an incomplete
 * body is a 400 naming the offending fields. Also normalizes the telephone by stripping
 * every non-digit character and requires exactly ten digits, rejecting otherwise with a
 * 400; the normalized value is stored on the body so it is persisted and returned.
 * Publishes the validated body for later steps.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidOwnerTelephoneException {
        List<String> missing = new ArrayList<>();
        checkField("firstName", request.getFirstName(), missing);
        checkField("lastName", request.getLastName(), missing);
        checkField("address", request.getAddress(), missing);
        checkField("city", request.getCity(), missing);
        checkField("telephone", request.getTelephone(), missing);
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        String telephone = request.getTelephone().replaceAll("\\D", "");
        if (telephone.length() != 10) {
            throw new InvalidOwnerTelephoneException(telephone);
        }
        request.setTelephone(telephone);
        validated.set(request);
    }

    private static void checkField(String name, String value, List<String> missing) {
        if (value == null || value.isBlank()) {
            missing.add(name);
        }
    }
}
