package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerFieldsInvalidException;

/**
 * Validates an optional {@code postcode} WHEN PRESENT, then leaves the (already published) body
 * untouched. When absent the owner is accepted unchanged, so the request contract stays
 * backward-compatible. When present the postcode must be exactly 4 digits and — when the owner's
 * city maps to a known region via {@link OwnerMapper#CITY_REGION} (Sydney-&gt;NSW, Melbourne-&gt;VIC,
 * Brisbane-&gt;QLD) — must fall within that region's fixed inclusive range (NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099). A city with no known region accepts any 4-digit postcode. A
 * malformed or out-of-range postcode is rejected with 400.
 */
public class ValidatePostcode {

    private static final Pattern FOUR_DIGITS = Pattern.compile("^[0-9]{4}$");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    public void service(@Val OwnerFieldsDto request) throws OwnerFieldsInvalidException {
        String postcode = request.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // optional when absent
        }
        postcode = postcode.trim();
        if (!FOUR_DIGITS.matcher(postcode).matches()) {
            throw new OwnerFieldsInvalidException(List.of("postcode"));
        }
        String region = OwnerMapper.CITY_REGION.get(request.getCity());
        int[] range = region == null ? null : REGION_POSTCODES.get(region);
        if (range != null) {
            int value = Integer.parseInt(postcode);
            if (value < range[0] || value > range[1]) {
                throw new OwnerFieldsInvalidException(List.of("postcode"));
            }
        }
    }
}
