package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Locality (canonical region) for pet owners. For a created owner the region is read straight off
 * the REGION prefix of its {@code customerCode} (see {@link AssignCustomerCode}), so the locality
 * shown on responses is exactly the region baked into the region-and-hash identity. When no such
 * code is present (e.g. legacy owners with no customer code) it falls back to resolving the region
 * from the owner's postcode first, using fixed inclusive postcode ranges: {@code NSW 2000-2099},
 * {@code VIC 3000-3099}, {@code QLD 4000-4099}; when the postcode is absent or falls in no known
 * range, it falls back to a fixed city-to-region table: {@code Sydney -> NSW},
 * {@code Melbourne -> VIC}, {@code Brisbane -> QLD}. Anything unresolved yields {@code UNKNOWN}.
 * Preferring the postcode returns the same region for known cities but disambiguates cities that
 * share a name. Derived purely from the owner's own state, so it carries no stored data and is
 * seed-independent. Used by the owner mapper to expose {@code locality} on responses.
 */
public final class Locality {

    /** City -> canonical region. Fixed, pinned reference data. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** The canonical regions a customer code may carry, used to recognise its REGION prefix. */
    private static final Set<String> KNOWN_REGIONS = Set.of("NSW", "VIC", "QLD", "UNKNOWN");

    /** Region -> inclusive 4-digit postcode range {low, high}. Fixed, pinned reference data. */
    private static final Map<String, int[]> REGION_RANGES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private Locality() {
    }

    /**
     * The canonical region for the given owner, read from the REGION prefix of its
     * {@code customerCode} when present and falling back to the postcode-then-city derivation
     * otherwise. This is the mapping the owner mapper uses so the exposed locality matches the
     * region-and-hash identity.
     */
    public static String of(Owner owner) {
        return of(owner.getCustomerCode(), owner.getPostcode(), owner.getCity());
    }

    /**
     * The canonical region for an owner, read from the REGION prefix of its {@code customerCode}
     * when present and falling back to the postcode-then-city derivation otherwise. This is the
     * mapping the owner mapper uses so the exposed locality matches the region-and-hash identity.
     */
    public static String of(String customerCode, String postcode, String city) {
        String region = CustomerCode.region(customerCode);
        return region != null && KNOWN_REGIONS.contains(region) ? region : of(postcode, city);
    }

    /**
     * The canonical region for the given owner, resolved from the postcode first and falling back
     * to the city when the postcode is absent or in no known range. Yields {@code UNKNOWN} when
     * neither resolves.
     */
    public static String of(String postcode, String city) {
        String byPostcode = fromPostcode(postcode);
        return byPostcode != null ? byPostcode : of(city);
    }

    /**
     * The canonical region for the given city, or {@code UNKNOWN} when the city is not in the
     * fixed table.
     */
    public static String of(String city) {
        return CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /** The region whose range contains the postcode, or {@code null} when absent or out of range. */
    private static String fromPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_RANGES.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }
}
