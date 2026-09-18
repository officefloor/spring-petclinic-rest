package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerFieldsException;

/**
 * Runs after {@link NormalizeOwnerTelephone} in {@code POST /api/owners}. Rejects a body
 * that is missing or blank in any required field ({@code firstName}, {@code lastName},
 * {@code address}, {@code city}, {@code telephone}), and also applies the DTO's declared
 * format constraints. When any field is invalid it throws
 * {@link InvalidOwnerFieldsException} naming those fields, so the response is a 400
 * listing them; otherwise it publishes the validated body for the downstream build step.
 * It reads the already-normalized body as a variable rather than binding {@code @RequestBody}.
 */
public class ValidateOwnerFields {

    /** Required fields in the order they should be reported, paired with their accessor. */
    private static Map<String, Supplier<String>> requiredFields(OwnerFieldsDto request) {
        Map<String, Supplier<String>> fields = new LinkedHashMap<>();
        fields.put("firstName", request::getFirstName);
        fields.put("lastName", request::getLastName);
        fields.put("address", request::getAddress);
        fields.put("city", request::getCity);
        fields.put("telephone", request::getTelephone);
        return fields;
    }

    public void service(@Val OwnerFieldsDto request, Validator validator,
            Out<OwnerFieldsDto> validated) throws InvalidOwnerFieldsException {

        // Preserve field-declaration order and de-duplicate fields flagged more than once.
        Set<String> invalid = new LinkedHashSet<>();

        // Required fields must be present and non-blank.
        requiredFields(request).forEach((name, accessor) -> {
            String value = accessor.get();
            if (value == null || value.isBlank()) {
                invalid.add(name);
            }
        });

        // Present fields must still satisfy the DTO's declared constraints (size, pattern, ...).
        for (ConstraintViolation<OwnerFieldsDto> violation : validator.validate(request)) {
            invalid.add(violation.getPropertyPath().toString());
        }

        if (!invalid.isEmpty()) {
            throw new InvalidOwnerFieldsException(new ArrayList<>(invalid));
        }

        validated.set(request);
    }
}
