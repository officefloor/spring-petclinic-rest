package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerEmailInvalidException;
import org.springframework.samples.petclinic.rest.escalation.OwnerFieldsMissingException;
import org.springframework.samples.petclinic.rest.escalation.OwnerTelephoneInvalidException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners} and the single step that binds the request body. Rejects a
 * body that is missing or blank in any required field (firstName, lastName, address, city, telephone)
 * with a 400 whose {@code errors} array lists the offending field names. A whitespace-only value is
 * treated as blank — the DTO's {@code @Size(min = 1)} alone would accept it — so this check, not bean
 * validation, is the authoritative implementation of the required-field rule.
 *
 * <p>The telephone is then normalized by stripping every non-digit character and must contain
 * exactly ten digits; otherwise the create is rejected with a 400 via
 * {@link OwnerTelephoneInvalidException}. The normalized ten-digit value is written back onto the
 * body so it is what gets stored and returned.
 *
 * <p>Email is optional. When absent (null) it is left untouched. When present it must be a
 * syntactically valid address; otherwise the create is rejected with a 400 via
 * {@link OwnerEmailInvalidException}. A valid email is lower-cased and written back onto the body so
 * the lower-cased value is what gets stored and returned. The validated body is republished for
 * {@link BuildOwner}.
 */
public class ValidateOwner {

    /** Pragmatic syntactic check: non-empty local part, '@', non-empty domain containing a dot. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsMissingException, OwnerTelephoneInvalidException,
            OwnerEmailInvalidException {
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
            throw new OwnerFieldsMissingException(missing);
        }
        String digits = OwnerTelephone.normalize(request.getTelephone());
        if (digits.length() != 10) {
            throw new OwnerTelephoneInvalidException(request.getTelephone());
        }
        request.setTelephone(digits);

        String email = request.getEmail();
        if (email != null) {
            if (!EMAIL.matcher(email).matches()) {
                throw new OwnerEmailInvalidException(email);
            }
            request.setEmail(email.toLowerCase());
        }

        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
