package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for an owner's {@code REGION} code — the geographic segment of
 * the owner's {@code customerCode} identity ({@code <REGION>-<HASH8>}) and, through it,
 * the owner's locality.
 *
 * <p>The region is derived from the postcode first: a 4-digit postcode falling within a
 * known region's inclusive range yields that region. When the postcode is absent or in no
 * known range, it falls back to the {@link #CITY_REGION} table, returning {@code UNKNOWN}
 * when the city is not listed.
 */
public final class OwnerRegion {

    private OwnerRegion() {
    }

    /** Fixed city-to-region table used as the fallback when the postcode does not resolve a region. */
    static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region-to-inclusive 4-digit postcode range {low, high}. */
    static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    /** Fixed region-to-IANA-timezone table. */
    static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney",
            "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    /** Whether the region code is one of the known regions (NSW, VIC, QLD). */
    public static boolean isKnownRegion(String region) {
        return region != null && REGION_POSTCODES.containsKey(region);
    }

    /** The IANA timezone name for a region code, or {@code null} when the region has no mapping. */
    public static String timezoneOfRegion(String region) {
        return region == null ? null : REGION_TIMEZONE.get(region);
    }

    /** The owner's region code, derived from the postcode (city table as fallback). */
    public static String regionOf(Owner owner) {
        String postcode = owner.getPostcode();
        if (postcode != null && postcode.matches("[0-9]{4}")) {
            int value = Integer.parseInt(postcode);
            for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
                int[] range = entry.getValue();
                if (value >= range[0] && value <= range[1]) {
                    return entry.getKey();
                }
            }
        }
        return CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * Extracts the {@code REGION} segment from a {@code customerCode} of the form
     * {@code <REGION>-<HASH8>} (the layout produced by
     * {@link AssignOwnerCustomerCode}). Returns {@code null} when the code is
     * {@code null}; returns the whole code when it carries no separator.
     */
    public static String regionFromCustomerCode(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int dash = customerCode.indexOf('-');
        return dash >= 0 ? customerCode.substring(0, dash) : customerCode;
    }
}
