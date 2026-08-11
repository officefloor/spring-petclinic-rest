package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's REGION code — the single source of truth now shared by both the region prefix
 * of the {@code memberId} and the owner's locality. The postcode decides first (NSW 2000-2099,
 * VIC 3000-3099, QLD 4000-4099); when the postcode is absent or in no known range, the fixed
 * city-to-region table ({@link OwnerMapper#CITY_REGION}: Sydney-&gt;NSW, Melbourne-&gt;VIC,
 * Brisbane-&gt;QLD) is consulted, falling back to {@code UNKNOWN} for an unlisted city.
 */
public final class OwnerRegion {

    /**
     * The fixed version-2 tag mixed into the region code as it appears INSIDE the identifiers. It is
     * intentionally NOT a plain letters-only region, so a v2 identifier's region can never coincide
     * with a v1 (plain-region) one.
     */
    public static final String VERSION_TAG = "V2";

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private OwnerRegion() {
    }

    /** The owner's region code: postcode range first, then the city table, then {@code UNKNOWN}. */
    public static String of(Owner owner) {
        String byPostcode = fromPostcode(owner.getPostcode());
        if (byPostcode != null) {
            return byPostcode;
        }
        return OwnerMapper.CITY_REGION.getOrDefault(owner.getCity(), "UNKNOWN");
    }

    /**
     * The region code as it appears INSIDE the version-2 identifiers: the plain {@link #of(Owner)
     * region} with the fixed {@link #VERSION_TAG 'V2'} tag mixed in (e.g. {@code "NSWV2"}). This is
     * used only to build identifiers (the {@code memberId} region prefix); the user-facing
     * {@code locality}, {@code timezone} and owner-segment region keep the plain region, never this.
     */
    public static String identityRegion(Owner owner) {
        return of(owner) + VERSION_TAG;
    }

    /**
     * Maps a postcode to its region by range, or {@code null} when the postcode is absent,
     * non-numeric or in no known range.
     */
    private static String fromPostcode(String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        int code;
        try {
            code = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException ex) {
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
