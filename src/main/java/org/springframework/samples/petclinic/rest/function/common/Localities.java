package org.springframework.samples.petclinic.rest.function.common;

import java.util.Map;

/**
 * Derives an owner's locality (canonical region). The postcode is preferred: the region is looked
 * up by postcode range first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), and only when the
 * postcode is absent or falls in no known range does it fall back to the fixed city-to-region
 * table. Anything unresolved maps to {@link #UNKNOWN}.
 *
 * <p>{@link #ofPostcode(String)} resolves the region from the postcode alone — the {@code REGION}
 * segment of an owner's {@code customerCode} — and {@link #regionOf(String)} reads that segment
 * back out of a stored {@code customerCode}.
 */
public final class Localities {

    /** Value returned when neither the postcode nor the city resolves a region. */
    public static final String UNKNOWN = "UNKNOWN";

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION = Map.of(
            "Sydney", "NSW",
            "Melbourne", "VIC",
            "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
            "NSW", new int[] {2000, 2099},
            "VIC", new int[] {3000, 3099},
            "QLD", new int[] {4000, 4099});

    private Localities() {
    }

    /**
     * The canonical region for {@code city} using only the city-to-region table, or
     * {@code UNKNOWN} when not in the table.
     */
    public static String of(String city) {
        return city == null ? UNKNOWN : CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /**
     * The canonical region, preferring the postcode: resolves by postcode range first and only
     * falls back to the city-to-region table when the postcode is absent or in no known range.
     * Returns {@code UNKNOWN} when neither resolves.
     */
    public static String of(String city, String postcode) {
        String byPostcode = byPostcode(postcode);
        return byPostcode != null ? byPostcode : of(city);
    }

    /**
     * The canonical region derived from the postcode ALONE (no city fallback): the region whose
     * range contains {@code postcode}, or {@code UNKNOWN} when the postcode is absent or in no known
     * range. This is the {@code REGION} segment of an owner's {@code customerCode}.
     */
    public static String ofPostcode(String postcode) {
        String byPostcode = byPostcode(postcode);
        return byPostcode != null ? byPostcode : UNKNOWN;
    }

    /**
     * The region an owner belongs to, read back from the {@code REGION} segment of its
     * {@code customerCode} ({@code <REGION>-<HASH8>}) — the single region-and-hash identity all
     * region-derived values now flow from. Returns {@code UNKNOWN} when the code is absent or
     * unsegmented.
     */
    public static String regionOf(String customerCode) {
        if (customerCode == null) {
            return UNKNOWN;
        }
        int dash = customerCode.indexOf('-');
        return dash < 0 ? UNKNOWN : customerCode.substring(0, dash);
    }

    /** The region whose range contains {@code postcode}, or {@code null} when none does. */
    private static String byPostcode(String postcode) {
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
}
