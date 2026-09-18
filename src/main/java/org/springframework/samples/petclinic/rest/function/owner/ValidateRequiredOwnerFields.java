package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create-owner request that is missing or blank in any required field, before
 * {@link BuildOwner} runs. Collects every offending field so the response reports them all
 * at once. Publishes the validated body for later steps, since the HTTP body is read once.
 */
public class ValidateRequiredOwnerFields {

    /** Required fields, paired with their accessor, in the order reported. */
    private static final List<Map.Entry<String, Function<OwnerFieldsDto, String>>> REQUIRED = List.of(
            Map.entry("firstName", OwnerFieldsDto::getFirstName),
            Map.entry("lastName", OwnerFieldsDto::getLastName),
            Map.entry("address", OwnerFieldsDto::getAddress),
            Map.entry("city", OwnerFieldsDto::getCity),
            Map.entry("telephone", OwnerFieldsDto::getTelephone));

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, Function<OwnerFieldsDto, String>> field : REQUIRED) {
            if (!StringUtils.hasText(field.getValue().apply(request))) {
                missing.add(field.getKey());
            }
        }
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        validated.set(request);
    }
}
