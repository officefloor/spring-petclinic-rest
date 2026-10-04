package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Single source of truth for the owner locality rule shared across the {@code POST /owners}
 * pipeline. The region is derived from the <em>postcode range</em> first (NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099), falling back to a fixed city-to-region table
 * (Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD) only when the postcode is absent or in no
 * known range; any other input has no known region. Resolving by postcode first returns the same
 * region for the known cities but disambiguates cities that share a name.
 *
 * <p>Centralised here (as {@link OwnerAddress} is for address normalisation) so the read-time and
 * write-time sides of the rule cannot drift: {@code OwnerMapper.locality} derives the returned
 * {@code locality} from it, and {@link ValidateOwnerPostcode} uses it to decide which region's
 * postcode range a supplied postcode must fall within.
 */
public final class OwnerLocality {

    /** City -> canonical region. Anything not listed has no known region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private OwnerLocality() {
    }

    /**
     * The canonical region for the given city, or {@code null} when the city is not in the
     * table (no known region).
     */
    public static String regionForCity(String city) {
        return city == null ? null : CITY_REGION.get(city);
    }

    /**
     * The canonical region whose postcode range contains the given postcode, or {@code null} when
     * the postcode is absent, not four digits, or in no known range.
     */
    public static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /** The region's inclusive 4-digit postcode range {low, high}, or {@code null} when none. */
    public static int[] postcodeRange(String region) {
        return region == null ? null : REGION_POSTCODES.get(region);
    }

    /**
     * The owner's canonical region, preferring the postcode's region and falling back to the
     * city-to-region table when the postcode is absent or in no known range. Returns {@code null}
     * when neither yields a known region. Callers that need a display sentinel map {@code null}
     * themselves (e.g. to {@code "UNKNOWN"}).
     */
    public static String regionFor(String postcode, String city) {
        String region = regionForPostcode(postcode);
        return region != null ? region : regionForCity(city);
    }
}
