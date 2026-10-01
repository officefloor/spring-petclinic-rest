package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerFieldsMissingException;
import org.springframework.samples.petclinic.rest.escalation.OwnerTelephoneInvalidException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners} and the single step that binds the request body. Rejects a
 * body that is missing or blank in any required field (firstName, lastName, address, city, telephone)
 * with a 400 whose {@code errors} array lists the offending field names. A whitespace-only value is
 * treated as blank — the DTO's {@code @Size(min = 1)} alone would accept it — so this check, not bean
 * validation, is the authoritative implementation of the required-field rule.
 *
 * <p>The telephone is then normalized by stripping every non-digit character and must contain
 * exactly ten digits; otherwise the create is rejected with a 400 via
 * {@link OwnerTelephoneInvalidException}. The normalized ten-digit value is written back onto the
 * body so it is what gets stored and returned. The validated body is republished for
 * {@link BuildOwner}.
 */
public class ValidateOwner {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsMissingException, OwnerTelephoneInvalidException {
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
            throw new OwnerFieldsMissingException(missing);
        }
        String digits = OwnerTelephone.normalize(request.getTelephone());
        if (digits.length() != 10) {
            throw new OwnerTelephoneInvalidException(request.getTelephone());
        }
        request.setTelephone(digits);
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
