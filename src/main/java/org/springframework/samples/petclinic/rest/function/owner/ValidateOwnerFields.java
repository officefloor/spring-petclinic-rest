package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerFieldsInvalidException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create request that is missing or blank in any required owner field, before
 * {@link BuildOwner} runs. Reads the body once and republishes it for later steps.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsInvalidException {
        List<String> errors = new ArrayList<>();
        checkRequired("firstName", request.getFirstName(), errors);
        checkRequired("lastName", request.getLastName(), errors);
        checkRequired("address", request.getAddress(), errors);
        checkRequired("city", request.getCity(), errors);
        checkRequired("telephone", request.getTelephone(), errors);
        normalizeTelephone(request, errors);
        if (!errors.isEmpty()) {
            throw new OwnerFieldsInvalidException(errors);
        }
        validated.set(request);
    }

    private static void checkRequired(String field, String value, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(field);
        }
    }

    /**
     * Strips every non-digit character from the telephone and requires exactly 10 digits,
     * storing the normalized value back on the request so later steps persist and return it.
     * Anything other than 10 digits after stripping is a 400 (adds a "telephone" error).
     */
    private static void normalizeTelephone(OwnerFieldsDto request, List<String> errors) {
        String telephone = request.getTelephone();
        if (telephone == null) {
            return;
        }
        String digits = telephone.replaceAll("\\D", "");
        if (digits.length() == 10) {
            request.setTelephone(digits);
        }
        else if (!errors.contains("telephone")) {
            errors.add("telephone");
        }
    }
}
