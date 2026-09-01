package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's locality from the pinned city-to-region table
 * (Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD), or {@code UNKNOWN}
 * for any other city.
 */
public final class Locality {

    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private Locality() {
    }

    public static String of(Owner owner) {
        String byPostcode = fromPostcode(owner.getPostcode());
        if (byPostcode != null) {
            return byPostcode;
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    private static String fromPostcode(String postcode) {
        if (postcode == null || !postcode.matches("\\d{4}")) {
            return null;
        }
        int code = Integer.parseInt(postcode);
        if (code >= 2000 && code <= 2099) {
            return "NSW";
        }
        if (code >= 3000 && code <= 3099) {
            return "VIC";
        }
        if (code >= 4000 && code <= 4099) {
            return "QLD";
        }
        return null;
    }
}
