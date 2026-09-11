package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.RequiredFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create request that is missing or blank in any required owner field,
 * before {@link BuildOwner} runs. The names of the offending fields are reported
 * as a 400 by {@link org.springframework.samples.petclinic.rest.escalation.RequiredFieldsExceptionHandler}.
 *
 * <p>This is a manual guard rather than {@code @Valid} so that a missing or blank
 * required field yields the {@code errors} field-name array this endpoint promises,
 * instead of the generic schema-validation problem detail.
 */
public class RequireOwnerFields {

    /** A pragmatic syntactic email check: a local part, an '@', and a dotted domain,
     *  none containing whitespace or a second '@'. */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws RequiredFieldsException {
        List<String> errors = new ArrayList<>();
        checkField("firstName", request.getFirstName(), errors);
        checkField("lastName", request.getLastName(), errors);
        checkField("address", request.getAddress(), errors);
        checkField("city", request.getCity(), errors);
        checkField("telephone", request.getTelephone(), errors);
        if (!errors.isEmpty()) {
            throw new RequiredFieldsException(errors);
        }
        // Normalize the telephone: strip every non-digit, then require exactly 10 digits.
        String telephone = request.getTelephone().replaceAll("\\D", "");
        if (telephone.length() != 10) {
            throw new RequiredFieldsException(List.of("telephone"));
        }
        request.setTelephone(telephone);
        // Email is optional; when present it must be syntactically valid and is
        // stored and returned lower-cased.
        String email = request.getEmail();
        if (email != null && !email.isBlank()) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new RequiredFieldsException(List.of("email"));
            }
            request.setEmail(email.toLowerCase());
        }
        validated.set(request);
    }

    private static void checkField(String name, String value, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(name);
        }
    }
}
