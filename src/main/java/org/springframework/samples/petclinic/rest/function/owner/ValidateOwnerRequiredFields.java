package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create request that is missing or blank in any required owner field, before
 * {@link BuildOwner} runs. Binds the body without {@code @Valid} so the missing fields are
 * reported as an {@code errors} array (via {@link MissingOwnerFieldsException}) rather than as
 * bean-validation schema errors. This is the only step that binds the body; it republishes it
 * for {@link BuildOwner} to consume.
 *
 * <p>The address is normalized in place first (see {@link AddressNormalizer}). An owner may supply its
 * address either in structured form ({@code addressLine1} with an optional {@code addressLine2}) or via
 * the flat {@code address} field (backward-compatible); the structured fields are preferred when present,
 * falling back to the flat address. The required-field check then rejects an owner that supplies neither.
 * The flat {@code address} is republished as the normalized composed value — the normalized
 * {@code addressLine1}, with a single space and the normalized {@code addressLine2} appended when
 * {@code addressLine2} is present — so the stored/returned value and every later address read see it.
 */
public class ValidateOwnerRequiredFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException {
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        String line2 = AddressNormalizer.normalize(request.getAddressLine2());
        String flat = AddressNormalizer.normalize(request.getAddress());

        // Prefer the structured addressLine1; fall back to the flat address.
        String effectiveLine1 = !line1.isEmpty() ? line1 : flat;
        String composed = line2.isEmpty() ? effectiveLine1 : effectiveLine1 + " " + line2;

        request.setAddressLine1(effectiveLine1.isEmpty() ? null : effectiveLine1);
        request.setAddressLine2(line2.isEmpty() ? null : line2);
        request.setAddress(composed);

        List<String> missing = new ArrayList<>();
        if (isBlank(request.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            missing.add("lastName");
        }
        if (isBlank(composed)) {
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
