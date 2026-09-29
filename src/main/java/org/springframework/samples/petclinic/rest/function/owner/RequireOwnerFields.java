package org.springframework.samples.petclinic.rest.function.owner;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Runs after {@link ValidateOwner} in the create-owner pipeline and enforces the one rule the DTO's
 * declarative constraints cannot express: no required field may be <em>blank</em>. {@code address} and
 * {@code city} carry only {@code @Size(min = 1)} (no pattern), so a whitespace-only value passes schema
 * validation; this step rejects it with a 400 listing every offending field.
 *
 * <p>Layered on top of {@code @Valid} rather than replacing it: null/empty/pattern violations are still
 * caught upstream by {@link ValidateOwner} (consistent with {@code PUT /owners/{ownerId}}), and both
 * paths surface the offending field names in the response {@code errors} array.
 */
public class RequireOwnerFields {

    public void service(@Val OwnerFieldsDto owner) throws MissingOwnerFieldsException {
        List<String> missing = new ArrayList<>();
        if (isBlank(owner.getFirstName())) {
            missing.add("firstName");
        }
        if (isBlank(owner.getLastName())) {
            missing.add("lastName");
        }
        // Valid when an address is supplied in EITHER form: a non-blank structured addressLine1 or the
        // flat address (kept for backward compatibility). NormalizeOwnerAddress has already composed the
        // structured lines into address, so a blank address here means neither form was supplied.
        if (!AddressNormalizer.hasStructuredAddress(owner.getAddressLine1()) && isBlank(owner.getAddress())) {
            missing.add("address");
        }
        if (isBlank(owner.getCity())) {
            missing.add("city");
        }
        if (isBlank(owner.getTelephone())) {
            missing.add("telephone");
        }
        if (!missing.isEmpty()) {
            throw new MissingOwnerFieldsException(missing);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
