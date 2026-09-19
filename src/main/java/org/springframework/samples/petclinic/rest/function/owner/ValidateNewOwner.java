package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create request that is missing or blank in any required owner field, before
 * {@link BuildOwner} maps the body to an entity. Runs first so the body is read once here
 * and republished for later steps. An address may be supplied in either form: a non-blank
 * structured {@code addressLine1}, or the flat {@code address} (judged blank after
 * normalization, see {@link AddressNormalizer}); a request supplying neither is rejected.
 */
public class ValidateNewOwner {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        List<String> missing = new ArrayList<>();
        checkPresent("firstName", request.getFirstName(), missing);
        checkPresent("lastName", request.getLastName(), missing);
        if (!hasAddress(request)) {
            missing.add("address");
        }
        checkPresent("city", request.getCity(), missing);
        checkPresent("telephone", request.getTelephone(), missing);
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        validated.set(request);
    }

    /** An address is present when a non-blank structured addressLine1 or a flat address is supplied. */
    private static boolean hasAddress(OwnerFieldsDto request) {
        return AddressNormalizer.isPresent(request.getAddressLine1())
                || !AddressNormalizer.normalize(request.getAddress()).isBlank();
    }

    private static void checkPresent(String name, String value, List<String> missing) {
        if (value == null || value.isBlank()) {
            missing.add(name);
        }
    }
}
