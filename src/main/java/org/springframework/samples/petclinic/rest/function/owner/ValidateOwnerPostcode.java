package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Validates the owner's optional postcode. When present it must be 4 digits and, for a city with a
 * known region ({@link Locality}), fall within that region's range (NSW 2000-2099, VIC 3000-3099,
 * QLD 4000-4099). A city with no known region accepts any 4-digit postcode. An absent postcode is
 * left untouched, so the create contract stays backward-compatible. An invalid postcode is a 400
 * via {@link MissingOwnerFieldsException}.
 */
public class ValidateOwnerPostcode {

    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    public void service(@Val Owner owner) throws MissingOwnerFieldsException {
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return;
        }
        int[] range = REGION_RANGE.get(Locality.of(owner));
        if (!postcode.matches("[0-9]{4}")
                || (range != null && (Integer.parseInt(postcode) < range[0] || Integer.parseInt(postcode) > range[1]))) {
            throw new MissingOwnerFieldsException(List.of("postcode"));
        }
    }
}
