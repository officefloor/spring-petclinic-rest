package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Validates the optional owner postcode. Runs only WHEN a postcode is present: an owner created
 * without a postcode is accepted unchanged (the request contract stays backward-compatible).
 *
 * <p>When present, the postcode must be exactly four digits. For a city with a known region it must
 * also fall within that region's inclusive range (Sydney/NSW 2000-2099, Melbourne/VIC 3000-3099,
 * Brisbane/QLD 4000-4099); a city with no known region accepts any 4-digit postcode. Anything else
 * is rejected with {@link InvalidPostcodeException} (400). The value is stored and returned as
 * given, so this step only validates and does not mutate the body.
 */
public class RequirePostcode {

    private static final Pattern FOUR_DIGITS = Pattern.compile("^[0-9]{4}$");

    /** City -> canonical region (matching the mapper's locality table). */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    public void service(@Val OwnerFieldsDto request) throws InvalidPostcodeException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // optional when absent
        }
        if (!FOUR_DIGITS.matcher(postcode).matches()) {
            throw new InvalidPostcodeException(postcode);
        }
        String region = CITY_REGION.get(request.getCity());
        int[] range = region == null ? null : REGION_RANGE.get(region);
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new InvalidPostcodeException(postcode);
            }
        }
    }
}
