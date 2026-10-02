package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Validates a new owner before {@link BuildOwner} runs: every one of firstName, lastName, address,
 * city and telephone must be present and non-blank. Any that are missing or blank are collected and
 * reported together as a {@link MissingOwnerFieldsException} (a 400), so the body is never persisted.
 *
 * <p>Deliberately does <em>not</em> use {@code @Valid}: bean validation would raise a
 * {@code MethodArgumentNotValidException} with a different response shape, whereas this rule must
 * answer with an {@code errors} array naming each offending field.
 */
public class ValidateNewOwner {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        // This list mirrors OwnerFieldsDto's required fields by hand: it must track the schema's
        // `required` set. We check presence/non-blankness only, a narrower rule than the DTO's full
        // constraints (@Size/@Pattern), so bean validation is intentionally not used here.
        List<String> missing = new ArrayList<>();
        require(missing, "firstName", request.getFirstName());
        require(missing, "lastName", request.getLastName());
        require(missing, "address", request.getAddress());
        require(missing, "city", request.getCity());
        require(missing, "telephone", request.getTelephone());
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        validated.set(request);
    }

    private static void require(List<String> missing, String field, String value) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }
}
