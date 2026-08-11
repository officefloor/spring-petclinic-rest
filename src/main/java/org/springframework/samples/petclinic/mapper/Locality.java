package org.springframework.samples.petclinic.mapper;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's locality (canonical <em>plain</em> region). Locality is the user-facing region
 * — {@code 'NSW'}, {@code 'VIC'}, {@code 'QLD'} or {@code 'UNKNOWN'} — and is <strong>not</strong>
 * an identifier: it never carries the version-2 tag that the identity derivation (see
 * {@link RegionCode}) mixes into the region <em>inside</em> the {@code memberId}. The region is
 * derived directly from the owner: the postcode is preferred: a 4-digit postcode falling in a known
 * region's range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099) fixes the region, which disambiguates
 * cities that share a name; only when the postcode is absent or in no known range does it fall back
 * to the fixed city-to-region table. Because it is derived independently of the memberId, locality
 * (and the {@link Timezone} and {@link OwnerSegment} that read it) stay the plain region even after
 * the identity moved to version 2. Kept out of {@link OwnerMapper} so MapStruct does not mistake the
 * helper for an implicit mapping method and apply it to every string property.
 */
public final class Locality {

    /** City -> canonical region; anything not listed derives locality "UNKNOWN". */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099},
        "VIC", new int[] {3000, 3099},
        "QLD", new int[] {4000, 4099});

    private Locality() {
    }

    /**
     * The owner's plain locality, derived directly from its city and postcode. This is the
     * user-facing region and deliberately does <em>not</em> read the memberId, whose embedded region
     * now carries the version-2 tag; keeping the two separate is what lets locality stay {@code 'NSW'}
     * while the identity moved to version 2.
     */
    public static String of(Owner owner) {
        return of(owner.getCity(), owner.getPostcode());
    }

    /**
     * The canonical region, preferring the postcode range and falling back to the
     * city-to-region table. Returns "UNKNOWN" when neither resolves a region.
     */
    public static String of(String city, String postcode) {
        String byPostcode = regionForPostcode(postcode);
        return byPostcode != null ? byPostcode : of(city);
    }

    /** The canonical region for {@code city}, or "UNKNOWN" when it is not in the table. */
    public static String of(String city) {
        return city == null ? "UNKNOWN" : CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * The region whose range contains {@code postcode}, or {@code null} when the postcode
     * is absent, non-numeric, or in no known range.
     */
    private static String regionForPostcode(String postcode) {
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
        for (Map.Entry<String, int[]> entry : REGION_POSTCODES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }
}
