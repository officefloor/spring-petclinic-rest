package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidPostcodeException;

/**
 * Validates an owner's postcode WHEN PRESENT, against the fixed per-region range derived from
 * the owner's city (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). A city with no known region
 * accepts any 4-digit postcode. When absent the postcode is optional, so the step is a no-op
 * and the owner is still accepted — the request contract stays backward-compatible.
 *
 * <p>The 4-digit syntactic format is also enforced by the {@code postcode} bean-validation pattern
 * on the request DTO (so a malformed value is normally a 400 before this step); this step
 * re-checks the format so a malformed postcode is rejected even for a city with no known region,
 * then enforces the semantic region range. It runs on the built {@link Owner} before it is saved.
 */
public class CheckOwnerPostcode {

    /** A valid postcode is exactly 4 digits. */
    private static final java.util.regex.Pattern FOUR_DIGITS = java.util.regex.Pattern.compile("\\d{4}");

    public void service(@Val Owner owner) throws InvalidPostcodeException {
        String postcode = owner.getPostcode();
        if (postcode == null || postcode.isBlank()) {
            return; // optional when absent
        }
        String region = Locality.of(owner.getCity());
        if (!FOUR_DIGITS.matcher(postcode).matches()) {
            throw new InvalidPostcodeException(postcode, owner.getCity(), region);
        }
        int[] range = Locality.postcodeRange(region);
        if (range == null) {
            return; // city with no known region accepts any 4-digit postcode
        }
        int value = Integer.parseInt(postcode);
        if (value < range[0] || value > range[1]) {
            throw new InvalidPostcodeException(postcode, owner.getCity(), region);
        }
    }
}
