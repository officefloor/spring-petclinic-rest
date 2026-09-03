package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DisposableEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}: rejects a request that is missing or blank in any of
 * firstName, lastName, address, city or telephone. A whitespace-only value counts as blank, which
 * bean validation alone does not catch for the pattern-less address/city fields, so the check is
 * explicit here. The address is normalized first (see {@link OwnerAddress}) and the required-field
 * check rejects an address that is blank after normalization; the normalized value is written back
 * so it is stored, returned and used for every later address comparison. The telephone is then
 * normalized to E.164 form (see {@link OwnerTelephone}); the
 * normalized value is written back so it is stored and returned, and a value that cannot form valid
 * E.164 is a 400. The
 * optional email, when present, must be a syntactically valid address and is normalized to lower
 * case; a present-but-invalid email is a 400. On success it republishes the body for {@link BuildOwner}.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidTelephoneException, InvalidEmailException,
            DisposableEmailException, InvalidPostcodeException {
        // Normalize whichever address fields are supplied, preferring the structured form. The stored,
        // returned 'address' is the composed value (normalized addressLine1, plus a space and the
        // normalized addressLine2 when present), falling back to the flat 'address'.
        String line1 = OwnerAddress.normalize(request.getAddressLine1());
        String line2 = OwnerAddress.normalize(request.getAddressLine2());
        request.setAddressLine1(line1.isEmpty() ? null : line1);
        request.setAddressLine2(line2.isEmpty() ? null : line2);
        request.setAddress(OwnerAddress.compose(request.getAddressLine1(),
                request.getAddressLine2(), request.getAddress()));
        List<String> missing = new ArrayList<>();
        require("firstName", request.getFirstName(), missing);
        require("lastName", request.getLastName(), missing);
        // Valid when an address is supplied in EITHER form; the composed value is blank only when both
        // the structured addressLine1 and the flat address are missing.
        require("address", request.getAddress(), missing);
        require("city", request.getCity(), missing);
        require("telephone", request.getTelephone(), missing);
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        request.setTelephone(OwnerTelephone.toE164(request.getTelephone()));
        request.setEmail(OwnerEmail.normalize(request.getEmail()));
        request.setPostcode(Postcode.normalize(request.getPostcode(), request.getCity()));
        validated.set(request);
    }

    private static void require(String field, String value, List<String> missing) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }
}
