package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's canonical region ('locality'). The postcode is preferred: when it falls
 * within a known region's inclusive range ({@code NSW 2000-2099}, {@code VIC 3000-3099},
 * {@code QLD 4000-4099}) that region wins. Only when the postcode is absent, malformed or in no
 * known range does it fall back to the fixed city-to-region table. This returns the same region
 * for known cities but disambiguates cities that share a name.
 *
 * <p>Kept as a standalone helper (referenced from {@link OwnerMapper}'s
 * {@code locality} expression) rather than a mapper {@code default} method: a
 * {@code String}-to-{@code String} method on the mapper interface would be
 * picked up by MapStruct as an automatic conversion for every String property.
 */
final class Locality {

    /** City -&gt; canonical region. Anything not listed derives {@link #UNKNOWN}. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -&gt; inclusive 4-digit postcode range {@code {low, high}}. */
    private static final Map<String, int[]> REGION_RANGE = Map.of(
            "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    static final String UNKNOWN = "UNKNOWN";

    private Locality() {
    }

    /**
     * The canonical region for the owner, preferring the postcode. When {@code postcode} is a
     * 4-digit value inside a known region's range that region is returned; otherwise the
     * {@code city} is looked up in the city-to-region table, deriving {@code "UNKNOWN"} when it is
     * not listed.
     */
    static String of(String postcode, String city) {
        String byPostcode = byPostcode(postcode);
        if (byPostcode != null) {
            return byPostcode;
        }
        return city == null ? UNKNOWN : CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /** The region whose range contains {@code postcode}, or {@code null} when none does. */
    private static String byPostcode(String postcode) {
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
