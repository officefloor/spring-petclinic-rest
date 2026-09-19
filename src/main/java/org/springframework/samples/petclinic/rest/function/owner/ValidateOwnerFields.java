package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Rejects a create-owner body that is missing or blank in any required field before
 * {@link BuildOwner} runs. Runs after {@link NormalizeOwnerAddress} has canonicalized the
 * body, so the {@code address} check sees the effective address — the composed structured
 * form when {@code addressLine1} was supplied, otherwise the normalized flat address — and
 * an owner supplying an address in either form (plus a city) is accepted.
 */
public class ValidateOwnerFields {

    public void service(@Val OwnerFieldsDto request) throws MissingOwnerFieldsException {
        List<String> missing = new ArrayList<>();
        require(missing, "firstName", request.getFirstName());
        require(missing, "lastName", request.getLastName());
        require(missing, "address", request.getAddress());
        require(missing, "city", request.getCity());
        require(missing, "telephone", request.getTelephone());
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
    }

    private static void require(List<String> missing, String field, String value) {
        if (value == null || value.isBlank()) {
            missing.add(field);
        }
    }
}
