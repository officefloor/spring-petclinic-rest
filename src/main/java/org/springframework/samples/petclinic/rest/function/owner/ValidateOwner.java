package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Binds the owner body and rejects it when any required field is missing or blank, so an
 * invalid body is a 400 (listing the offending field names) even when the owner does not exist.
 * Also normalizes the telephone to E.164 form (see {@link TelephoneE164}), publishing the
 * normalized value so it is stored and returned as {@code telephone}.
 * The optional email is accepted when absent; when present it must be a syntactically valid
 * address (else 400) and is lower-cased so it is stored and returned as {@code email}.
 * Runs before {@link LoadOwner}/{@link BuildOwner} and publishes the body for later steps.
 */
public class ValidateOwner {

    /** Syntactic email check: a non-empty local part, an '@', then a dotted domain. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$");

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidTelephoneException, InvalidEmailException {
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
        // Normalize telephone to E.164; reject anything that cannot form a valid number.
        String e164 = TelephoneE164.normalize(request.getTelephone());
        if (e164 == null) {
            throw new InvalidTelephoneException(request.getTelephone());
        }
        request.setTelephone(e164);
        // Email is optional. When present it must be syntactically valid; store it lower-cased.
        String email = request.getEmail();
        if (email != null && !email.isBlank()) {
            String normalized = email.trim().toLowerCase();
            if (!EMAIL.matcher(normalized).matches()) {
                throw new InvalidEmailException(email);
            }
            request.setEmail(normalized);
        }
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
