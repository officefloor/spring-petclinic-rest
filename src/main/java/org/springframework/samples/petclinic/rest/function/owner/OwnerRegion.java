package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * The single source of truth for an owner's canonical region, derived from the postcode and city. A
 * region is a short code (NSW, VIC, QLD) fixed by two lookup tables: the inclusive 4-digit postcode
 * range per region, and the city-to-region fallback used when the postcode yields nothing.
 *
 * <p>{@link #fromPostcode} answers the region for a postcode alone (or {@code null}); {@link #locality}
 * layers the city fallback on top, matching how a whole owner resolves to a locality.
 */
public final class OwnerRegion {

    /** City -> canonical region, from the fixed city-to-region table. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private OwnerRegion() {
    }

    /** The canonical region for {@code city} from the fixed city-to-region table, or {@code null}. */
    static String regionForCity(String city) {
        return city == null ? null : CITY_REGION.get(city);
    }

    /** The inclusive 4-digit postcode range {low, high} for {@code region}, or {@code null}. */
    static int[] rangeForRegion(String region) {
        return REGION_RANGE.get(region);
    }

    /**
     * The region whose fixed range contains {@code postcode} (NSW 2000-2099, VIC 3000-3099,
     * QLD 4000-4099), or {@code null} when the postcode is absent, non-numeric, or in no known range.
     */
    public static String fromPostcode(String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException ex) {
            return null;
        }
        for (Map.Entry<String, int[]> entry : REGION_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The owner's locality (region), preferring the postcode: the region whose fixed range contains the
     * postcode, else the fixed city-to-region table (Sydney->NSW, Melbourne->VIC, Brisbane->QLD), else
     * {@code UNKNOWN} when neither yields a region.
     */
    public static String locality(String postcode, String city) {
        String fromPostcode = fromPostcode(postcode);
        if (fromPostcode != null) {
            return fromPostcode;
        }
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }
}
