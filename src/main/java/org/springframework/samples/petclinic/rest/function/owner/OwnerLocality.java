package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for an owner's derived {@code locality} (region code). Resolution prefers
 * the postcode: a 4-digit postcode in a known range maps to its region (NSW 2000-2099, VIC
 * 3000-3099, QLD 4000-4099). When the postcode is absent or in no known range, it falls back to the
 * city-to-region table (Sydney/NSW, Melbourne/VIC, Brisbane/QLD). Anything unresolved is
 * {@code "UNKNOWN"}. Preferring the postcode disambiguates cities that share a name.
 */
public final class OwnerLocality {

    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private OwnerLocality() {
    }

    public static String region(Owner owner) {
        String byPostcode = byPostcode(owner.getPostcode());
        if (byPostcode != null) {
            return byPostcode;
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    private static String byPostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        if (value >= 2000 && value <= 2099) {
            return "NSW";
        }
        if (value >= 3000 && value <= 3099) {
            return "VIC";
        }
        if (value >= 4000 && value <= 4099) {
            return "QLD";
        }
        return null;
    }
}
