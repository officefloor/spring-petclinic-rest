package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerPostcodeException;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Runs first in the create-owner pipeline. Rejects a request that is missing or blank in any of
 * firstName, lastName, address, city or telephone by throwing {@link MissingOwnerFieldsException}
 * with the names of every offending field. Otherwise it publishes the body for later steps.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidOwnerEmailException, InvalidOwnerTelephoneException,
            InvalidOwnerPostcodeException, FutureRegistrationDateException {
        // Normalize the address before the required-field check, so an address that is blank after
        // normalization is rejected and later steps see (and store) the canonical form.
        OwnerAddress.normalize(request);
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
        // Store the telephone in E.164 form; reject a number that cannot form valid E.164.
        OwnerTelephone.normalize(request);
        // An owner may include an email; when present it must be valid and is stored lower-cased.
        OwnerEmail.normalize(request);
        // An owner may include a postcode; when present it must be 4 digits valid for the city's region.
        OwnerPostcode.validate(request);
        // An owner may supply a registrationDate; when present it must not be later than the server date.
        OwnerRegistrationDate.validate(request);
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
