package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}. Rejects a body that is missing (absent) or blank
 * (whitespace only) in any of firstName, lastName, address, city or telephone — {@code @Size(min = 1)}
 * on the DTO does not catch a whitespace-only value, so the check is explicit here. The remaining
 * schema constraints (pattern, max length) are validated too, so a bad value is still a 400.
 *
 * <p>Binds the body directly (no {@code @Valid}) so that a missing field surfaces as this rule's
 * {@code errors} response rather than the generic schema-validation escalation, and publishes the
 * body for {@link BuildOwner} to consume.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Validator validator, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        List<String> errors = new ArrayList<>();
        addIfBlank(errors, "firstName", request.getFirstName());
        addIfBlank(errors, "lastName", request.getLastName());
        addIfBlank(errors, "address", request.getAddress());
        addIfBlank(errors, "city", request.getCity());
        normalizeTelephone(errors, request);
        for (ConstraintViolation<OwnerFieldsDto> violation : validator.validate(request)) {
            String field = violation.getPropertyPath().toString();
            if (!field.isEmpty() && !errors.contains(field)) {
                errors.add(field);
            }
        }
        if (!errors.isEmpty()) {
            throw new MissingOwnerFieldsException(errors);
        }
        normalizeEmail(request);
        validated.set(request);
    }

    /**
     * Email is optional. When absent nothing is done; when present the schema {@code @Email}
     * constraint (checked above) has already rejected a syntactically invalid address with a
     * {@code 400}, so a value reaching here is valid and is stored and returned lower-cased.
     */
    private static void normalizeEmail(OwnerFieldsDto request) {
        String email = request.getEmail();
        if (email != null) {
            request.setEmail(email.toLowerCase(java.util.Locale.ROOT));
        }
    }

    private static void addIfBlank(List<String> errors, String name, String value) {
        if (value == null || value.isBlank()) {
            errors.add(name);
        }
    }

    /**
     * Normalize the telephone by removing every non-digit character, then require exactly 10 digits.
     * The stripped, 10-digit value is written back onto the request so it is stored and returned as
     * {@code telephone}. Anything that is not exactly 10 digits after stripping is a {@code telephone}
     * error (400).
     */
    private static void normalizeTelephone(List<String> errors, OwnerFieldsDto request) {
        String value = request.getTelephone();
        String digits = value == null ? "" : value.replaceAll("\\D", "");
        if (digits.length() != 10) {
            errors.add("telephone");
            return;
        }
        request.setTelephone(digits);
    }
}
