package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.RequiredFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create request that is missing or blank in any required owner field,
 * before {@link BuildOwner} runs. The names of the offending fields are reported
 * as a 400 by {@link org.springframework.samples.petclinic.rest.escalation.RequiredFieldsExceptionHandler}.
 *
 * <p>This is a manual guard rather than {@code @Valid} so that a missing or blank
 * required field yields the {@code errors} field-name array this endpoint promises,
 * instead of the generic schema-validation problem detail.
 */
public class RequireOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws RequiredFieldsException {
        List<String> errors = new ArrayList<>();
        checkField("firstName", request.getFirstName(), errors);
        checkField("lastName", request.getLastName(), errors);
        checkField("address", request.getAddress(), errors);
        checkField("city", request.getCity(), errors);
        checkField("telephone", request.getTelephone(), errors);
        if (!errors.isEmpty()) {
            throw new RequiredFieldsException(errors);
        }
        // Normalize the telephone: strip every non-digit, then require exactly 10 digits.
        String telephone = request.getTelephone().replaceAll("\\D", "");
        if (telephone.length() != 10) {
            throw new RequiredFieldsException(List.of("telephone"));
        }
        request.setTelephone(telephone);
        validated.set(request);
    }

    private static void checkField(String name, String value, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(name);
        }
    }
}
