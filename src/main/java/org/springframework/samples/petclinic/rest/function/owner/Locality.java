package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Derives an owner's {@code locality}: the canonical region. The postcode is preferred — a postcode
 * that falls in a known range fixes the region (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099, via
 * {@link PostcodeRanges}) regardless of the city. Only when the postcode is absent or in no known
 * range does it fall back to the fixed city-to-region table (Sydney -> NSW, Melbourne -> VIC,
 * Brisbane -> QLD). Any city not in the table (including a null or blank city) derives
 * {@code "UNKNOWN"}. This returns the same region for known cities but disambiguates cities that
 * share a name. The mapping is pinned so it is stable across requests and needs no persisted column.
 */
public final class Locality {

    /** City -> canonical region; anything not listed derives locality "UNKNOWN". */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    static final String UNKNOWN = "UNKNOWN";

    private Locality() {
    }

    /** The region for {@code city} from the fixed table alone, or {@code "UNKNOWN"} when unlisted. */
    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * Derives the region, preferring the postcode: the region whose range contains the postcode wins;
     * otherwise the city-to-region table decides.
     */
    public static String of(String city, String postcode) {
        String fromPostcode = PostcodeRanges.regionFor(postcode);
        if (fromPostcode != null) {
            return fromPostcode;
        }
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * The locality read back off the owner's {@code memberId} identity ({@code <REGION><FY><HASH8><CHK>},
     * see {@link AssignMemberId}): the leading run of letters (the REGION segment, which is followed by
     * the two-digit FY). Returns {@code "UNKNOWN"} when the id is null or carries no region segment, so
     * locality now flows from the same region-and-hash identity rather than being derived independently.
     */
    public static String ofMemberId(String memberId) {
        if (memberId == null) {
            return UNKNOWN;
        }
        int i = 0;
        while (i < memberId.length() && Character.isLetter(memberId.charAt(i))) {
            i++;
        }
        if (i == 0) {
            return UNKNOWN;
        }
        return memberId.substring(0, i);
    }
}
