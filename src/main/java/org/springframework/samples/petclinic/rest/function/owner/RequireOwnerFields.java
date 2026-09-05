package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * First step of {@code POST /api/owners}: rejects a body that is missing or blank in any
 * required field (firstName, lastName, address, city, telephone) before {@link BuildOwner}
 * runs, so such a request is a 400 listing the offending fields rather than a persisted owner.
 *
 * <p>Reads the raw body (no {@code @Valid}) so this check runs ahead of bean validation and
 * owns the response format; the body is republished for later steps.
 *
 * <p>The address is normalized in place first (see {@link OwnerAddress}) so the required check
 * rejects an address that is blank <em>after</em> normalization, and every later step sees — and
 * the response returns — the normalized, composed address. An owner is valid when it supplies an
 * address in EITHER form: a non-blank structured {@code addressLine1} or the flat {@code address}.
 */
public class RequireOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        OwnerAddress.normalizeInPlace(request);
        List<String> missing = new ArrayList<>();
        if (isBlank(request.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            missing.add("lastName");
        }
        if (!OwnerAddress.hasAddress(request)) {
            missing.add("address");
        }
        if (isBlank(request.getCity())) {
            missing.add("city");
        }
        if (isBlank(request.getTelephone())) {
            missing.add("telephone");
        }
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
