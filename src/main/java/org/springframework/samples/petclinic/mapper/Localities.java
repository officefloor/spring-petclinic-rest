package org.springframework.samples.petclinic.mapper;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's canonical locality (region). The postcode range is preferred:
 * a postcode within a known region range decides the region and disambiguates cities
 * that share a name. Only when the postcode is absent or in no known range does the
 * fixed city-to-region table apply. Kept as a plain static helper (not a mapper
 * method) so MapStruct does not treat it as an implicit String-to-String mapping method.
 */
public final class Localities {

    /** City -> canonical region; anything not listed derives locality "UNKNOWN". */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private Localities() {
    }

    /** The canonical region for the given city, or "UNKNOWN" when the city is not in the table. */
    public static String region(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * The canonical region. The postcode range is looked up first; if the postcode is
     * absent or in no known range, falls back to the city-to-region table. Returns
     * "UNKNOWN" when neither resolves.
     */
    public static String region(String city, String postcode) {
        String byPostcode = regionByPostcode(postcode);
        if (byPostcode != null) {
            return byPostcode;
        }
        return region(city);
    }

    /**
     * The owner's locality, taken from the region component of its region-and-hash
     * {@code customerCode} ({@code <REGION>-<HASH8>}) — so the locality is the same
     * identity the customerCode is built from. Falls back to the shared
     * {@link #region(String, String)} derivation for owners without a customerCode.
     */
    public static String region(Owner owner) {
        String fromCode = regionOfCode(owner.getCustomerCode());
        if (fromCode != null) {
            return fromCode;
        }
        return region(owner.getCity(), owner.getPostcode());
    }

    /** The REGION prefix of a {@code <REGION>-<HASH8>} customerCode, or null when absent. */
    private static String regionOfCode(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int dash = customerCode.indexOf('-');
        return dash < 0 ? customerCode : customerCode.substring(0, dash);
    }

    /** The region whose postcode range contains the given postcode, or null if none. */
    private static String regionByPostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        int code;
        try {
            code = Integer.parseInt(postcode.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (code >= range[0] && code <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }
}
