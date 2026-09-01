package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Region resolution for owner identity. {@link #forPostcode} maps an owner to a region code (the
 * {@code REGION} half of the {@code <REGION>-<HASH8>} customerCode), preferring the postcode: a
 * 4-digit postcode in a known range maps to its region (NSW 2000-2099, VIC 3000-3099, QLD
 * 4000-4099); otherwise it falls back to the city-to-region table (Sydney/NSW, Melbourne/VIC,
 * Brisbane/QLD), and anything unresolved is {@code "UNKNOWN"}. The derived {@code locality} now
 * reads that same region back off the customerCode (see {@link #region}).
 */
public final class OwnerLocality {

    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private OwnerLocality() {
    }

    /** Region for the owner's identity, derived from the postcode with the city as fallback. */
    public static String forPostcode(String postcode, String city) {
        String byPostcode = byPostcode(postcode);
        if (byPostcode != null) {
            return byPostcode;
        }
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /** Locality: the region carried in the {@code <REGION>-<HASH8>} customerCode identity. */
    public static String region(Owner owner) {
        String code = owner.getCustomerCode();
        int dash = code == null ? -1 : code.indexOf('-');
        return dash < 0 ? "UNKNOWN" : code.substring(0, dash);
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
