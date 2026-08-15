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
 * First step of {@code POST /api/owners}. Normalizes the address to its canonical form (see
 * {@link OwnerAddress}) and stores it back on the request, then rejects a request that is missing
 * or blank in any required field (firstName, lastName, address, city, telephone) — the address
 * being tested for blankness after normalization — with a 400 whose body lists the offending field
 * names. Then normalizes the telephone to E.164 form (see {@link
 * TelephoneE164}), storing the E.164 value back on the request so later steps persist and return
 * it; a telephone that cannot form valid E.164 is rejected with a 400. Runs before {@link
 * BuildOwner} maps the body to an {@link org.springframework.samples.petclinic.model.Owner}.
 */
public class ValidateNewOwner {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidTelephoneException, InvalidEmailException {
        request.setAddress(OwnerAddress.normalize(request.getAddress()));
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
        request.setTelephone(TelephoneE164.normalize(request.getTelephone()));
        request.setEmail(OwnerEmail.normalize(request.getEmail()));
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
