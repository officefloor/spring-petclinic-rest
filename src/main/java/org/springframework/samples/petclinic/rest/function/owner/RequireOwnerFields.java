package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create request that is missing or blank in any required owner field, before
 * {@link BuildOwner} runs. Publishes the body so the single {@code @RequestBody} binding lives here.
 */
public class RequireOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        request.setAddress(effectiveAddress(request));
        List<String> missing = new ArrayList<>();
        require("firstName", request.getFirstName(), missing);
        require("lastName", request.getLastName(), missing);
        require("address", request.getAddress(), missing);
        require("city", request.getCity(), missing);
        require("telephone", request.getTelephone(), missing);
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        validated.set(request);
    }

    /**
     * The address to store: the composed normalized structured address (normalized addressLine1,
     * plus a single space and the normalized addressLine2 when present) when a non-blank addressLine1
     * is supplied, otherwise the normalized flat {@code address} (backward compatible).
     */
    private static String effectiveAddress(OwnerFieldsDto request) {
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        if (line1.isEmpty()) {
            return AddressNormalizer.normalize(request.getAddress());
        }
        String line2 = AddressNormalizer.normalize(request.getAddressLine2());
        return line2.isEmpty() ? line1 : line1 + " " + line2;
    }

    private static void require(String name, String value, List<String> missing) {
        if (value == null || value.isBlank()) {
            missing.add(name);
        }
    }
}
