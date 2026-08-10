package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerEmailDisposableException;
import org.springframework.samples.petclinic.rest.escalation.OwnerEmailInvalidException;
import org.springframework.samples.petclinic.rest.escalation.OwnerFieldsRequiredException;
import org.springframework.samples.petclinic.rest.escalation.OwnerPostcodeInvalidException;
import org.springframework.samples.petclinic.rest.escalation.OwnerRegistrationDateFutureException;
import org.springframework.samples.petclinic.rest.escalation.OwnerTelephoneInvalidException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}. Rejects a request that is missing or blank in any of
 * firstName, lastName, address, city or telephone, collecting every offending field name so the
 * response can list them all. The address is normalized first (see
 * {@link OwnerAddress#normalize(String)}) and rejected when it is blank after normalization. It then
 * normalizes the telephone into E.164 form (see {@link OwnerTelephone#toE164(String)}), storing the
 * normalized address and telephone back on the body so they are persisted and
 * returned. On success it publishes the body for {@link BuildOwner} to consume
 * (only one step may bind {@code @RequestBody}).
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsRequiredException, OwnerTelephoneInvalidException, OwnerEmailInvalidException,
            OwnerEmailDisposableException, OwnerPostcodeInvalidException, OwnerRegistrationDateFutureException {
        // A supplied registration date cannot be in the future — reject one later than today.
        LocalDate registrationDate = request.getRegistrationDate();
        LocalDate serverDate = LocalDate.now();
        if (registrationDate != null && registrationDate.isAfter(serverDate)) {
            throw new OwnerRegistrationDateFutureException(registrationDate, serverDate);
        }
        List<String> errors = new ArrayList<>();
        require("firstName", request.getFirstName(), errors);
        require("lastName", request.getLastName(), errors);
        // Normalize the address up front so the required check rejects one that is blank after
        // normalization, and the normalized value is what gets stored and returned.
        String normalizedAddress = OwnerAddress.normalize(request.getAddress());
        if (normalizedAddress == null) {
            errors.add("address");
        }
        require("city", request.getCity(), errors);
        require("telephone", request.getTelephone(), errors);
        if (!errors.isEmpty()) {
            throw new OwnerFieldsRequiredException(errors);
        }
        // Postcode is optional; when present it must be 4 digits and valid for the city's region.
        OwnerPostcode.validate(request.getPostcode(), request.getCity());
        request.setAddress(normalizedAddress);
        request.setTelephone(OwnerTelephone.toE164(request.getTelephone()));
        request.setEmail(OwnerEmail.normalize(request.getEmail()));
        validated.set(request);
    }

    private static void require(String field, String value, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(field);
        }
    }
}
