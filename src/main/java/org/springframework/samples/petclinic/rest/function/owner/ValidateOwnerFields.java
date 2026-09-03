package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}: rejects a request that is missing or blank in any of
 * firstName, lastName, address, city or telephone. A whitespace-only value counts as blank, which
 * bean validation alone does not catch for the pattern-less address/city fields, so the check is
 * explicit here. The telephone is then normalized by stripping every non-digit character and must
 * be exactly 10 digits; the normalized value is written back so it is stored and returned. On
 * success it republishes the body for {@link BuildOwner}.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidTelephoneException {
        List<String> missing = new ArrayList<>();
        require("firstName", request.getFirstName(), missing);
        require("lastName", request.getLastName(), missing);
        require("address", request.getAddress(), missing);
        require("city", request.getCity(), missing);
        require("telephone", request.getTelephone(), missing);
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        String telephone = request.getTelephone().replaceAll("\\D", "");
        if (telephone.length() != 10) {
            throw new InvalidTelephoneException(request.getTelephone());
        }
        request.setTelephone(telephone);
        validated.set(request);
    }

    private static void require(String field, String value, List<String> missing) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }
}
