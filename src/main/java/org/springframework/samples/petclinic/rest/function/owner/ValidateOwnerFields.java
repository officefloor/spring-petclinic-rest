package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Rejects a create-owner request that is missing or blank in any required field
 * ({@code firstName}, {@code lastName}, address, {@code city}, {@code telephone}).
 *
 * <p>The address may be supplied in either form: the structured {@code addressLine1}
 * (with optional {@code addressLine2}) is preferred when a non-blank {@code addressLine1}
 * is given, otherwise the flat {@code address} is used — an owner is valid when it carries
 * an address in either form. The supplied lines are normalized (see
 * {@link AddressNormalizer}) and the composed, normalized {@code address} is stored back
 * (structured {@code addressLine1} plus a single space and {@code addressLine2} when
 * present); the flat form is kept accepted for backward compatibility.
 *
 * <p>Runs first in the {@code POST /api/owners} pipeline and binds the body without
 * {@code @Valid}, so a missing field surfaces as a {@link MissingOwnerFieldsException}
 * (400 with an {@code errors} array) rather than the generic schema-validation response.
 * The address is normalized first so the blank check rejects one that is empty after
 * normalization. It then normalizes the
 * telephone to E.164 form (see {@link TelephoneE164}), rejecting with an
 * {@link InvalidTelephoneException} (400) when it cannot form a valid E.164 number.
 * On success it stores the normalized address and E.164 telephone and republishes the
 * body for {@link BuildOwner}.
 */
public class ValidateOwnerFields {

    public void service(@RequestBody OwnerFieldsDto request, Out<OwnerFieldsDto> validated)
            throws MissingOwnerFieldsException, InvalidTelephoneException, InvalidEmailException {
        List<String> errors = new ArrayList<>();
        // Normalize whichever address fields were supplied. The structured lines are
        // preferred; the flat 'address' remains accepted for backward compatibility.
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        String line2 = AddressNormalizer.normalize(request.getAddressLine2());
        String flat = AddressNormalizer.normalize(request.getAddress());
        boolean hasStructured = !isBlank(line1);
        String address = hasStructured ? AddressNormalizer.compose(line1, line2) : flat;
        if (isBlank(request.getFirstName())) {
            errors.add("firstName");
        }
        if (isBlank(request.getLastName())) {
            errors.add("lastName");
        }
        // Valid with an address in EITHER form: a non-blank addressLine1 or flat address.
        if (isBlank(address)) {
            errors.add("address");
        }
        if (isBlank(request.getCity())) {
            errors.add("city");
        }
        if (isBlank(request.getTelephone())) {
            errors.add("telephone");
        }
        if (!errors.isEmpty()) {
            throw new MissingOwnerFieldsException(errors);
        }
        request.setAddressLine1(hasStructured ? line1 : null);
        request.setAddressLine2(hasStructured && !isBlank(line2) ? line2 : null);
        request.setAddress(address);
        String telephone = TelephoneE164.normalize(request.getTelephone());
        if (telephone == null) {
            throw new InvalidTelephoneException(
                    "Telephone cannot be normalized to a valid E.164 number");
        }
        request.setTelephone(telephone);
        request.setEmail(OwnerEmail.normalize(request.getEmail()));
        validated.set(request);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
