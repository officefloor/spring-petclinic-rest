package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's locality (region). The postcode range is preferred (NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099); when the postcode is absent or in no known range, the
 * city-to-region table (Sydney->NSW, Melbourne->VIC, Brisbane->QLD) is used instead. Any
 * city not listed yields "UNKNOWN".
 */
public final class Locality {

    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private Locality() {
    }

    public static String of(Owner owner) {
        String byPostcode = fromPostcode(owner.getPostcode());
        return byPostcode != null ? byPostcode : CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    private static String fromPostcode(String postcode) {
        if (postcode == null || !postcode.matches("\\d{4}")) {
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
