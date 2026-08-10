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
 * response can list them all. The address may be given in structured form (addressLine1 [+
 * addressLine2]) or via the legacy flat 'address' field; it is composed and normalized first (see
 * {@link OwnerAddress#compose(String, String, String)}) and rejected when neither form yields a
 * non-blank value. It then normalizes the telephone into E.164 form (see
 * {@link OwnerTelephone#toE164(String)}), storing the normalized address lines, composed address and
 * telephone back on the body so they are persisted and
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
        // An address may be supplied in structured form (addressLine1 [+ addressLine2]) or via the
        // legacy flat 'address' field; the structured form is preferred when present. Normalize each
        // supplied part up front so the required check rejects one that is blank after normalization
        // and the normalized values are what get stored and returned.
        String normalizedLine1 = OwnerAddress.normalize(request.getAddressLine1());
        String normalizedLine2 = OwnerAddress.normalize(request.getAddressLine2());
        String composedAddress =
                OwnerAddress.compose(request.getAddressLine1(), request.getAddressLine2(), request.getAddress());
        if (composedAddress == null) {
            errors.add("address");
        }
        require("city", request.getCity(), errors);
        require("telephone", request.getTelephone(), errors);
        if (!errors.isEmpty()) {
            throw new OwnerFieldsRequiredException(errors);
        }
        // Postcode is optional; when present it must be 4 digits and valid for the city's region.
        OwnerPostcode.validate(request.getPostcode(), request.getCity());
        request.setAddressLine1(normalizedLine1);
        request.setAddressLine2(normalizedLine2);
        request.setAddress(composedAddress);
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
