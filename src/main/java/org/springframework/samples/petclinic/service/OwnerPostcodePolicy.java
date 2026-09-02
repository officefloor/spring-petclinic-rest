package org.springframework.samples.petclinic.service;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Business rule: when an owner supplies a {@code postcode} it must be valid for the owner's city.
 * Validity is keyed by the city's region (Sydney->NSW, Melbourne->VIC, Brisbane->QLD): NSW
 * 2000-2099, VIC 3000-3099, QLD 4000-4099. A city with no known region accepts any 4-digit
 * postcode. Postcode is optional: an owner created without one is accepted unchanged. Kept as a
 * small, self-contained unit so the rule can be enforced from the create flow without adding
 * complexity to the controller, mapper or service.
 */
public final class OwnerPostcodePolicy {

    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private static final Map<String, int[]> REGION_RANGE = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private OwnerPostcodePolicy() {
    }

    /**
     * Reject the owner when it supplies a postcode that is out of range for its city's region.
     * A missing postcode, or a city whose region is unknown, passes unchanged.
     *
     * @param owner the owner being created
     * @throws InvalidPostcodeException if the postcode is out of range for the city's region
     */
    public static void rejectInvalidPostcode(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode == null) {
            return;
        }
        String region = CITY_REGION.get(owner.getCity());
        if (region == null) {
            return;
        }
        int[] range = REGION_RANGE.get(region);
        int code = Integer.parseInt(postcode);
        if (code < range[0] || code > range[1]) {
            throw new InvalidPostcodeException();
        }
    }

    /** Thrown when a supplied postcode is out of range for the city's region. */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidPostcodeException extends RuntimeException {
    }
}
