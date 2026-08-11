package org.springframework.samples.petclinic.util;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's canonical region ('locality'). The postcode is preferred: a
 * postcode falling in a known region's range ({@code NSW 2000-2099}, {@code VIC
 * 3000-3099}, {@code QLD 4000-4099}) determines the region directly. Only when the
 * postcode is absent or in no known range does derivation fall back to the fixed
 * city-to-region table. A city (or postcode) that resolves to no region derives the
 * locality {@code "UNKNOWN"}.
 */
public final class Localities {

    /** Fixed city -> canonical region table. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    /** Fixed region -> IANA timezone table. */
    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney",
            "VIC", "Australia/Melbourne",
            "QLD", "Australia/Brisbane");

    /** The locality returned when neither postcode nor city resolves to a region. */
    public static final String UNKNOWN = "UNKNOWN";

    private Localities() {
    }

    /**
     * Derives the owner's locality from its {@code customerCode} identity ({@code '<REGION>-<HASH8>'}),
     * whose REGION is the canonical region: the locality is the region encoded in the code. Owners
     * with no {@code customerCode} yet (e.g. seed data) fall back to deriving the region directly from
     * postcode and city.
     *
     * @param owner the owner (must not be {@code null}).
     * @return the canonical region for the owner, or {@code "UNKNOWN"} when none resolves.
     */
    public static String localityFor(Owner owner) {
        String code = owner.getCustomerCode();
        if (code != null) {
            int dash = code.indexOf('-');
            if (dash > 0) {
                return code.substring(0, dash);
            }
        }
        return localityFor(owner.getCity(), owner.getPostcode());
    }

    /**
     * Derives the owner's IANA timezone name from its locality/region via the fixed
     * region-to-timezone table ({@code NSW -> Australia/Sydney}, {@code VIC ->
     * Australia/Melbourne}, {@code QLD -> Australia/Brisbane}).
     *
     * @param owner the owner (must not be {@code null}).
     * @return the IANA timezone name, or {@code null} when the region is {@code "UNKNOWN"}
     *         (or otherwise not in the table).
     */
    public static String timezoneFor(Owner owner) {
        return REGION_TIMEZONE.get(localityFor(owner));
    }

    /**
     * Derives the region from the city alone. Equivalent to {@link #localityFor(String,
     * String)} with a {@code null} postcode.
     *
     * @param city the owner's city (may be {@code null}).
     * @return the canonical region for {@code city}, or {@code "UNKNOWN"} when the
     *         city is {@code null} or not in the fixed table.
     */
    public static String localityFor(String city) {
        return localityFor(city, null);
    }

    /**
     * Derives the region, preferring the postcode. The postcode is consulted first: if
     * it falls in a known region's range, that region is returned. Otherwise (postcode
     * absent, malformed, or out of every range) derivation falls back to the
     * city-to-region table.
     *
     * @param city     the owner's city (may be {@code null}).
     * @param postcode the owner's postcode (may be {@code null}).
     * @return the canonical region, or {@code "UNKNOWN"} when neither resolves.
     */
    public static String localityFor(String city, String postcode) {
        String byPostcode = regionForPostcode(postcode);
        if (byPostcode != null) {
            return byPostcode;
        }
        return city == null ? UNKNOWN : CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * @return the region whose range contains {@code postcode}, or {@code null} when
     *         the postcode is {@code null}, not a 4-digit number, or in no known range.
     */
    private static String regionForPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        for (Map.Entry<String, int[]> entry : REGION_RANGE.entrySet()) {
            int[] range = entry.getValue();
            if (value >= range[0] && value <= range[1]) {
                return entry.getKey();
            }
        }
        return null;
    }
}
