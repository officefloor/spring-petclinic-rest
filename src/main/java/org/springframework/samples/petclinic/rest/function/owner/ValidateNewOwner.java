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
 * Validates a new owner before {@link BuildOwner} runs: every one of firstName, lastName, address,
 * city and telephone must be present and non-blank. Any that are missing or blank are collected and
 * reported together as a {@link MissingOwnerFieldsException} (a 400), so the body is never persisted.
 *
 * <p>The telephone is then normalized by stripping every non-digit character and must be exactly ten
 * digits; otherwise an {@link InvalidTelephoneException} (a 400) is raised. The stripped value is
 * written back onto the request so downstream steps store and return the 10-digit telephone.
 *
 * <p>Deliberately does <em>not</em> use {@code @Valid}: bean validation would raise a
 * {@code MethodArgumentNotValidException} with a different response shape, whereas this rule must
 * answer with an {@code errors} array naming each offending field.
 */
public class ValidateNewOwner {

    /**
     * Syntactic email check (the HTML5/Hibernate-Validator {@code @Email} form): a local part of the
     * permitted characters, an {@code @}, then a dot-separated domain of label segments. Used only to
     * decide between accept and 400; a valid value is then lower-cased and written back onto the request.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?"
                    + "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidTelephoneException, InvalidEmailException {
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

        // Normalize the telephone: keep only digits, then require exactly ten. Store the stripped
        // value back so BuildOwner/SaveOwner persist it and the response echoes the 10-digit form.
        String normalized = OwnerTelephone.normalize(request.getTelephone());
        if (normalized.length() != 10) {
            throw new InvalidTelephoneException(normalized);
        }
        request.setTelephone(normalized);

        // Email is optional: only an email that is present (non-blank) must be a valid address. A valid
        // value is lower-cased and written back (like telephone above) so downstream steps store and
        // return the canonical form.
        String email = request.getEmail();
        if (email != null && !email.isBlank()) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new InvalidEmailException(email);
            }
            request.setEmail(email.toLowerCase());
        }

        validated.set(request);
    }

    private static void require(List<String> missing, String field, String value) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }
}
