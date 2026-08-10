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
 * Rejects a create-owner request that is missing or blank in any required field, before
 * {@link BuildOwner} runs. Each offending field name is collected and reported together as a
 * 400 whose {@code errors} array lists them (see {@link MissingOwnerFieldsException}).
 *
 * <p>Also normalizes the telephone to E.164 form (see {@link OwnerTelephone}): a request whose
 * telephone cannot form a valid E.164 number is rejected with a 400 (see
 * {@link InvalidTelephoneException}). The normalized E.164 value is stored back on the
 * request so it is persisted and returned as {@code telephone}.
 *
 * <p>Binds the request body for the pipeline and republishes it as a variable so that
 * {@link BuildOwner} can read it without a second {@code @RequestBody} binding.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidTelephoneException, InvalidEmailException {
        List<String> missing = new ArrayList<>();
        require(missing, "firstName", request.getFirstName());
        require(missing, "lastName", request.getLastName());
        require(missing, "address", request.getAddress());
        require(missing, "city", request.getCity());
        require(missing, "telephone", request.getTelephone());
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        request.setTelephone(OwnerTelephone.toE164(request.getTelephone()));
        OwnerEmail.normalize(request);
        validated.set(request);
    }

    private static void require(List<String> missing, String field, String value) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }
}
