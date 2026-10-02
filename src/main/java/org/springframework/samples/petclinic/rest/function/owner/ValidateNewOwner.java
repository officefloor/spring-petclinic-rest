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
 * First step of {@code POST /api/owners}. Rejects a body that is missing or blank in any
 * required field (firstName, lastName, an address in either form, city, telephone) with a 400
 * listing the offending field names, normalizes the telephone to E.164 form (400 if it cannot
 * form a valid E.164 number), then publishes the body for {@link BuildOwner}.
 *
 * <p>Runs before {@link BuildOwner} and binds {@code @RequestBody} without {@code @Valid}
 * so the required-field check produces the {@code errors} response shape rather than the
 * generic schema-validation shape, and so an invalid body is a 400 here.
 */
public class ValidateNewOwner {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidTelephoneException, InvalidEmailException {
        // Normalize the address up front so the required-field check sees the canonical form
        // (an address blank after normalization is rejected) and every later step — storage,
        // the response and household comparisons — carries the same normalized value. The
        // structured addressLine1/addressLine2 are preferred when supplied, falling back to the
        // flat address; the composed address is what the later steps carry.
        OwnerAddress.apply(request);
        List<String> missing = new ArrayList<>();
        if (isBlank(request.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            missing.add("lastName");
        }
        // Accepted in either form: a non-blank structured addressLine1, or the flat address.
        // OwnerAddress.apply has composed the effective address, so a blank address means
        // neither form supplied one.
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
        String e164 = OwnerTelephone.normalize(request.getTelephone());
        if (e164 == null) {
            throw new InvalidTelephoneException(
                    "Telephone is not a valid E.164 number: " + request.getTelephone());
        }
        request.setTelephone(e164);
        String email = OwnerEmail.normalize(request.getEmail());
        if (email != null && !OwnerEmail.isValid(email)) {
            throw new InvalidEmailException("Email is not a valid address: " + request.getEmail());
        }
        if (email != null && OwnerEmail.isDisposableDomain(email)) {
            throw new InvalidEmailException(
                    "Email domain is a disposable-domain blocklist entry: " + request.getEmail());
        }
        request.setEmail(email);
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
