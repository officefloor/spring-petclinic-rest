package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}. Rejects a body that is missing or blank in any
 * required field (firstName, lastName, address, city, telephone) with a 400 listing the
 * offending field names, normalizes the telephone to its 10 digits, then publishes the
 * body for {@link BuildOwner}.
 *
 * <p>Runs before {@link BuildOwner} and binds {@code @RequestBody} without {@code @Valid}
 * so the required-field check produces the {@code errors} response shape rather than the
 * generic schema-validation shape, and so an invalid body is a 400 here.
 */
public class ValidateNewOwner {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidTelephoneException {
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
        String digits = request.getTelephone().replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new InvalidTelephoneException(
                    "Telephone must be exactly 10 digits after stripping; got " + digits.length());
        }
        request.setTelephone(digits);
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
