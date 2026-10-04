package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Create-time guard for {@code POST /owners}: rejects a request that is missing or blank in
 * any required field (firstName, lastName, address, city, telephone) with a 400 whose body
 * lists the offending field names. Runs first in the pipeline and publishes the body for
 * {@link BuildOwner} once it is known to be complete.
 *
 * <p>Distinct from {@link ValidateOwner} (the {@code @Valid} declarative path used by
 * {@code PUT /owners/{ownerId}}) for two reasons the spec forces here:
 * <ul>
 * <li>the create spec requires the {@code errors} array shape
 *     ({@link MissingOwnerFieldsException}), not the {@code ProblemDetail} a bean-validation
 *     escalation produces; and</li>
 * <li>"blank" must be rejected, which the DTO constraints do not express — {@code city} and
 *     {@code address} carry only {@code @NotNull @Size(min = 1)}, so a whitespace-only value
 *     passes bean validation. Hence the explicit {@link #isBlank(String)} check and the
 *     deliberate absence of {@code @Valid} on the binding.</li>
 * </ul>
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        List<String> missing = new ArrayList<>();
        if (isBlank(request.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            missing.add("lastName");
        }
        // Address is normalised first (trim/collapse/upper-case/expand abbreviations) and the
        // required-field check runs against that form, so a value that is blank only once
        // normalised is rejected. The canonical value is written back in place so it is what
        // gets stored and returned and what the household checks later compare.
        String normalizedAddress = OwnerAddress.normalize(request.getAddress());
        if (normalizedAddress.isEmpty()) {
            missing.add("address");
        }
        else {
            request.setAddress(normalizedAddress);
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
