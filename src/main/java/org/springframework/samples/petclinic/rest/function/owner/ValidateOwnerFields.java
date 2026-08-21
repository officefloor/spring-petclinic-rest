package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create request that is missing or blank in any required owner field, so an
 * incomplete body is a 400 whose {@code errors} array names each offending field. Also
 * normalizes the telephone by stripping every non-digit character and requiring exactly
 * ten digits, republishing the normalized 10-digit value so it is stored and returned as
 * {@code telephone}; a telephone that is not ten digits after stripping is a 400 too.
 * The optional {@code email} is validated only when present: a syntactically invalid
 * address is a 400, otherwise it is normalized to lower-case and republished so it is
 * stored and returned lower-cased.
 * Runs first and republishes the body as a variable for {@link BuildOwner}.
 */
public class ValidateOwnerFields {

    /** Syntactic email check: one '@', non-empty local part, and a dotted domain. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingFieldsException {
        List<String> errors = new ArrayList<>();
        if (isBlank(request.getFirstName())) {
            errors.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            errors.add("lastName");
        }
        if (isBlank(request.getAddress())) {
            errors.add("address");
        }
        if (isBlank(request.getCity())) {
            errors.add("city");
        }
        String telephone = request.getTelephone() == null ? ""
                : request.getTelephone().replaceAll("\\D", "");
        if (telephone.length() != 10) {
            errors.add("telephone");
        }
        else {
            request.setTelephone(telephone);
        }
        String email = request.getEmail();
        if (email != null && !email.isBlank()) {
            String normalized = email.trim().toLowerCase();
            if (!EMAIL.matcher(normalized).matches()) {
                errors.add("email");
            }
            else {
                request.setEmail(normalized);
            }
        }
        if (!errors.isEmpty()) {
            throw new MissingFieldsException(errors);
        }
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
