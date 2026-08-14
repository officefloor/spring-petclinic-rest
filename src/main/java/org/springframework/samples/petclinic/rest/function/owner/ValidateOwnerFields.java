package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerFieldsValidationException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create-owner request that is missing or blank in any required field
 * (firstName, lastName, address, city, telephone) before {@link BuildOwner} runs.
 *
 * <p>Binds the body here (the single {@code @RequestBody} for the pipeline) and republishes it
 * so later steps read it via {@code @Val}. On failure it throws
 * {@link OwnerFieldsValidationException}, which the escalation handler turns into a 400 whose
 * {@code errors} array lists each offending field.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws OwnerFieldsValidationException {
        List<String> errors = new ArrayList<>();
        requireText("firstName", request.getFirstName(), errors);
        requireText("lastName", request.getLastName(), errors);
        requireText("address", request.getAddress(), errors);
        requireText("city", request.getCity(), errors);
        requireText("telephone", request.getTelephone(), errors);
        normalizeTelephone(request, errors);
        OwnerEmail.normalize(request, errors);
        if (!errors.isEmpty()) {
            throw new OwnerFieldsValidationException(errors);
        }
        validated.set(request);
    }

    /**
     * Strips every non-digit from the telephone and requires exactly 10 digits, storing the
     * normalized value back on the request so later steps persist and return it. A telephone that
     * is not exactly 10 digits after stripping adds a {@code telephone} error (400). Skipped when
     * the telephone is missing/blank, which {@link #requireText} has already flagged.
     */
    private static void normalizeTelephone(OwnerFieldsDto request, List<String> errors) {
        String telephone = request.getTelephone();
        if (telephone == null || telephone.isBlank()) {
            return;
        }
        String digits = telephone.replaceAll("\\D", "");
        if (digits.length() != 10) {
            errors.add("telephone");
            return;
        }
        request.setTelephone(digits);
    }

    private static void requireText(String field, String value, List<String> errors) {
        if (value == null || value.isBlank()) {
            errors.add(field);
        }
    }
}
