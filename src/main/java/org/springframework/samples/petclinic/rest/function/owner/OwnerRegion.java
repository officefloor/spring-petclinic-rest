package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for an owner's {@code REGION} code — the geographic segment of
 * the owner's {@code memberId} identity ({@code <REGION><FY><HASH8><CHK>}) and, through
 * it, the owner's locality.
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

    /** Length of the fixed trailing {@code <FY><HASH8><CHK>} segment of a memberId:
     *  2 (fiscal year) + 8 (hash) + 1 (Luhn check digit). */
    private static final int MEMBER_ID_SUFFIX_LENGTH = 11;

    /**
     * Extracts the {@code REGION} segment from a {@code memberId} of the form
     * {@code <REGION><FY><HASH8><CHK>} (the layout produced by
     * {@link AssignOwnerMemberId}), optionally followed by a {@code -<n>} collision
     * suffix. Returns {@code null} when the id is {@code null}; returns the whole id when
     * it is too short to carry the fixed trailing segment.
     */
    public static String regionFromMemberId(String memberId) {
        if (memberId == null) {
            return null;
        }
        int dash = memberId.indexOf('-');
        String base = dash >= 0 ? memberId.substring(0, dash) : memberId;
        return base.length() > MEMBER_ID_SUFFIX_LENGTH
                ? base.substring(0, base.length() - MEMBER_ID_SUFFIX_LENGTH) : base;
    }
}
