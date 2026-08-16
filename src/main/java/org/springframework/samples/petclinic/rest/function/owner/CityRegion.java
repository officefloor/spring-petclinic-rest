package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Fixed city-to-region lookup used to derive an owner's {@code locality}.
 *
 * <p>The table is intentionally closed: only Sydney, Melbourne and Brisbane map to a
 * canonical region. Any other (or missing) city derives the locality {@code "UNKNOWN"}.
 */
public final class CityRegion {

    /** City (case-insensitive) -> canonical region. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "sydney", "NSW",
        "melbourne", "VIC",
        "brisbane", "QLD");

    /** Region -> inclusive {low, high} 4-digit postcode range. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    /** Region -> IANA timezone name. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    /** Locality returned when the city is not in {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    private CityRegion() {
    }

    /** The canonical region for {@code city}, or {@code "UNKNOWN"} when it is not in the table. */
    public static String localityOf(String city) {
        if (city == null) {
            return UNKNOWN;
        }
        return CITY_REGION.getOrDefault(city.trim().toLowerCase(Locale.ROOT), UNKNOWN);
    }

    /**
     * The canonical region, preferring the {@code postcode}: the region whose range contains the
     * postcode is returned first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). Only when the
     * postcode is absent, non-numeric or in no known range does this fall back to the city-to-region
     * table. Returns {@code "UNKNOWN"} when neither yields a region.
     */
    public static String localityOf(String city, String postcode) {
        String byPostcode = regionForPostcode(postcode);
        return byPostcode != null ? byPostcode : localityOf(city);
    }

    /** The region whose inclusive range contains {@code postcode}, or {@code null} when none does. */
    private static String regionForPostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException ex) {
            return null;
        }
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * The inclusive {@code {low, high}} postcode range for {@code city}'s region, or {@code null}
     * when the city has no known region (in which case any 4-digit postcode is accepted).
     */
    public static int[] postcodeRangeOf(String city) {
        return REGION_POSTCODES.get(localityOf(city));
    }

    /**
     * An owner's {@code locality}, now the REGION segment of its {@code memberId}
     * ({@code <REGION><FY><HASH8><CHK>}). The region is the leading known region code (NSW, VIC, QLD
     * or UNKNOWN) the id starts with. When the id is absent or not in that shape (e.g. an owner not
     * created through the member-id pipeline), falls back to deriving the region from the city and
     * postcode directly.
     */
    public static String localityOfMemberId(String memberId, String city, String postcode) {
        if (memberId != null) {
            for (String region : REGION_POSTCODES.keySet()) {
                if (memberId.startsWith(region)) {
                    return region;
                }
            }
            if (memberId.startsWith(UNKNOWN)) {
                return UNKNOWN;
            }
        }
        return localityOf(city, postcode);
    }

    /**
     * The IANA timezone name for the owner's locality/region, derived the same way as
     * {@link #localityOfMemberId(String, String, String)}, via the fixed region-to-timezone
     * table (NSW->Australia/Sydney, VIC->Australia/Melbourne, QLD->Australia/Brisbane).
     * Returns {@code null} when the region is {@code "UNKNOWN"} (not in the table).
     */
    public static String timezoneOfMemberId(String memberId, String city, String postcode) {
        return REGION_TIMEZONE.get(localityOfMemberId(memberId, city, postcode));
    }
}
