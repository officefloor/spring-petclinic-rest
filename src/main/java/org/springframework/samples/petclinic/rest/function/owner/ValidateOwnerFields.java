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
 * First step of {@code POST /api/owners}: rejects a request that is missing or blank in any of
 * firstName, lastName, address, city or telephone. A whitespace-only value counts as blank, which
 * bean validation alone does not catch for the pattern-less address/city fields, so the check is
 * explicit here. The telephone is then normalized to E.164 form (see {@link OwnerTelephone}); the
 * normalized value is written back so it is stored and returned, and a value that cannot form valid
 * E.164 is a 400. The
 * optional email, when present, must be a syntactically valid address and is normalized to lower
 * case; a present-but-invalid email is a 400. On success it republishes the body for {@link BuildOwner}.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidTelephoneException, InvalidEmailException {
        List<String> missing = new ArrayList<>();
        require("firstName", request.getFirstName(), missing);
        require("lastName", request.getLastName(), missing);
        require("address", request.getAddress(), missing);
        require("city", request.getCity(), missing);
        require("telephone", request.getTelephone(), missing);
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        request.setTelephone(OwnerTelephone.toE164(request.getTelephone()));
        request.setEmail(OwnerEmail.normalize(request.getEmail()));
        validated.set(request);
    }

    private static void require(String field, String value, List<String> missing) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }
}
