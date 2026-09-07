package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.MissingFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}. Rejects a request that is missing or blank in any
 * required owner field before {@link BuildOwner} runs, throwing {@link MissingFieldsException}
 * (handled as a 400 listing the offending field names). Normalizes the address (see
 * {@link OwnerAddress}) before that check, so an address left blank once whitespace is collapsed is
 * rejected as missing. Normalizes the telephone to E.164 form (see {@link OwnerTelephone}), throwing
 * {@link InvalidTelephoneException} (400) when it cannot form a valid number. Publishes the
 * normalized body for later steps, so the stored, returned and compared address is the normalized
 * form.
 */
public class RequireOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingFieldsException, InvalidTelephoneException, InvalidEmailException {
        // Prefer the structured address when supplied, falling back to the flat 'address' for
        // backward compatibility. Store each supplied part in its normalized form and expose the
        // composed 'address' (normalized addressLine1, plus normalized addressLine2 after a single
        // space when present) so the required-field check, storage and every reader see one form.
        request.setAddressLine1(OwnerAddress.normalizeOrNull(request.getAddressLine1()));
        request.setAddressLine2(OwnerAddress.normalizeOrNull(request.getAddressLine2()));
        request.setAddress(OwnerAddress.compose(request.getAddressLine1(), request.getAddressLine2(),
                request.getAddress()));
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
            throw new MissingFieldsException(missing);
        }
        request.setTelephone(OwnerTelephone.toE164(request.getTelephone()));
        request.setEmail(OwnerEmail.normalize(request.getEmail()));
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
