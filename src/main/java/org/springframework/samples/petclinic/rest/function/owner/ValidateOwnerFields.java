package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}. Rejects a create whose firstName, lastName,
 * address, city or telephone is missing or blank with a 400 whose {@code errors} array
 * names each offending field. Bean validation still runs first (via {@code @Valid}) so
 * pattern/size failures keep their existing behaviour; this step additionally catches
 * whitespace-only values that slip past {@code @Size(min = 1)}. Publishes the validated
 * body for {@link BuildOwner}.
 */
@Validated
public class ValidateOwnerFields {

    public void service(@Valid @RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        List<String> missing = new ArrayList<>();
        requireText(missing, "firstName", request.getFirstName());
        requireText(missing, "lastName", request.getLastName());
        requireText(missing, "address", request.getAddress());
        requireText(missing, "city", request.getCity());
        requireText(missing, "telephone", request.getTelephone());
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        validated.set(request);
    }

    private static void requireText(List<String> missing, String field, String value) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }
}
