package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;
import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Rejects a supplied postcode that is not four digits, or that falls outside the
 * inclusive range fixed for the owner's city region (NSW 2000-2099, VIC 3000-3099,
 * QLD 4000-4099). A city with no known region accepts any four-digit postcode, and an
 * absent postcode is left untouched, so the request contract stays backward-compatible.
 */
public class ValidatePostcode {

    private static final Map<String, int[]> CITY_RANGE = Map.of(
            "Sydney", new int[] {2000, 2099},
            "Melbourne", new int[] {3000, 3099},
            "Brisbane", new int[] {4000, 4099});

    public void service(@Val Owner owner) throws MissingOwnerFieldsException {
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return;
        }
        int[] range = CITY_RANGE.get(owner.getCity());
        boolean valid = postcode.matches("[0-9]{4}") && (range == null
                || (Integer.parseInt(postcode) >= range[0] && Integer.parseInt(postcode) <= range[1]));
        if (!valid) {
            throw new MissingOwnerFieldsException(List.of("postcode"));
        }
    }
}
