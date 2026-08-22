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

    /** Region -> IANA timezone name; anything not listed derives a null timezone. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

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
     * The owner's locality, taken from the REGION component of its
     * {@code memberId} ({@code <REGION><FY><HASH8><CHK>}) — so the locality is the same
     * identity the memberId is built from. Falls back to the shared
     * {@link #region(String, String)} derivation for owners without a memberId.
     */
    public static String region(Owner owner) {
        String fromCode = regionOfCode(owner.getMemberId());
        if (fromCode != null) {
            return fromCode;
        }
        return region(owner.getCity(), owner.getPostcode());
    }

    /**
     * The owner's IANA timezone name, derived from its {@link #region(Owner) locality}
     * via the fixed region-to-timezone table. Returns null when the region has no known
     * timezone (e.g. "UNKNOWN").
     */
    public static String timezone(Owner owner) {
        return REGION_TIMEZONE.get(region(owner));
    }

    /**
     * The REGION prefix of a {@code <REGION><FY><HASH8><CHK>} memberId — the leading run
     * of letters before the two-digit fiscal year. Returns null when the memberId is
     * absent or has no leading letters.
     */
    private static String regionOfCode(String memberId) {
        if (memberId == null) {
            return null;
        }
        int i = 0;
        while (i < memberId.length() && Character.isLetter(memberId.charAt(i))) {
            i++;
        }
        return i == 0 ? null : memberId.substring(0, i);
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
