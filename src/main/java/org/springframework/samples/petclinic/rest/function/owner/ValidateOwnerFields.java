package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create-owner request that is missing or blank in any of firstName, lastName,
 * address, city or telephone, before {@link BuildOwner} runs. Runs first so an incomplete
 * body is a 400 naming the offending fields. Also normalizes the telephone into E.164 form
 * (see {@link OwnerTelephone}), rejecting otherwise with a 400; the normalized value is
 * stored on the body so it is persisted and returned.
 * When an {@code email} is present it must be a syntactically valid address and is stored
 * lower-cased (rejected with 400 otherwise); an absent email is allowed.
 * Publishes the validated body for later steps.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidOwnerTelephoneException, InvalidOwnerEmailException,
            InvalidOwnerPostcodeException, FutureRegistrationDateException {
        // Normalize the address up front so the required-field check below rejects an address that
        // is blank after normalization, and the persisted/returned value is the normalized form.
        request.setAddress(OwnerAddress.normalize(request.getAddress()));
        List<String> missing = new ArrayList<>();
        checkField("firstName", request.getFirstName(), missing);
        checkField("lastName", request.getLastName(), missing);
        checkField("address", request.getAddress(), missing);
        checkField("city", request.getCity(), missing);
        checkField("telephone", request.getTelephone(), missing);
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        request.setTelephone(OwnerTelephone.toE164(request.getTelephone()));
        OwnerEmail.normalize(request);
        OwnerPostcode.validate(request.getPostcode(), request.getCity());
        LocalDate registrationDate = request.getRegistrationDate();
        if (registrationDate != null && registrationDate.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException("registrationDate must not be later than the server date");
        }
        validated.set(request);
    }

    private static void checkField(String name, String value, List<String> missing) {
        if (value == null || value.isBlank()) {
            missing.add(name);
        }
    }
}
