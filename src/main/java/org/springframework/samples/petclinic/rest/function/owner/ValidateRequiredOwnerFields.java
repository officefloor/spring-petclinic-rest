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
 * The address is checked in its composed, normalized form (see {@link AddressNormalizer#compose}), so
 * a request is accepted when it supplies an address in either form — a non-blank {@code addressLine1}
 * or the flat {@code address} — and rejected when neither yields a non-blank address.
 */
public class ValidateRequiredOwnerFields {

    /** Required fields, paired with their accessor, in the order reported. */
    private static final List<Map.Entry<String, Function<OwnerFieldsDto, String>>> REQUIRED = List.of(
            Map.entry("firstName", OwnerFieldsDto::getFirstName),
            Map.entry("lastName", OwnerFieldsDto::getLastName),
            Map.entry("address", dto -> AddressNormalizer.compose(
                    dto.getAddressLine1(), dto.getAddressLine2(), dto.getAddress())),
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
