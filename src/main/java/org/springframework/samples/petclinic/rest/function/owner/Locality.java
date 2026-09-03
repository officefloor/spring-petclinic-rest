package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's locality (region code). It is the REGION segment embedded at the
 * start of the owner's {@code memberId}, which is {@link #region(Owner)}.
 */
public class Locality {

    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    public static String locality(Owner owner) {
        return region(owner);
    }

    /**
     * Region code derived from the postcode: a four-digit postcode inside a known range
     * wins (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099); otherwise it falls back to the
     * city-to-region table, which disambiguates cities that share a name.
     */
    public static String region(Owner owner) {
        String byPostcode = byPostcode(owner.getPostcode());
        return byPostcode != null ? byPostcode : CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    private static String byPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int p = Integer.parseInt(postcode);
        if (p >= 2000 && p <= 2099) return "NSW";
        if (p >= 3000 && p <= 3099) return "VIC";
        if (p >= 4000 && p <= 4099) return "QLD";
        return null;
    }
}
