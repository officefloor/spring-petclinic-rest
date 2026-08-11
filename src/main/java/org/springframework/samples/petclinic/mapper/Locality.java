package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's canonical region ('locality') from their postcode and city.
 *
 * <p>The postcode takes precedence: a 4-digit postcode falling in a known region's range
 * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099) resolves the region directly. Only when the
 * postcode is absent or in no known range does derivation fall back to the fixed city-to-region
 * table. This yields the same region for known cities but disambiguates cities sharing a name.
 *
 * <p>Kept as a standalone class (not a method on {@link OwnerMapper}) so MapStruct does
 * not mistake it for an implicit {@code String -> String} mapping method and apply it to
 * every string property.
 */
public final class Locality {

    /** Fixed city-to-region table. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -&gt; inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private Locality() {
    }

    /**
     * The canonical region derived from {@code postcode} first, falling back to {@code city}.
     * Returns {@code "UNKNOWN"} when neither resolves a region.
     */
    public static String of(String city, String postcode) {
        String byPostcode = regionForPostcode(postcode);
        return byPostcode != null ? byPostcode : of(city);
    }

    /** The canonical region for {@code city}, or {@code "UNKNOWN"} when it is not in the table. */
    public static String of(String city) {
        return city == null ? "UNKNOWN" : CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * The region carried by a {@code '<REGION>-<HASH8>'} customer code &mdash; its prefix up to the
     * first {@code '-'}. This is now the single source of an owner's locality, so the value derives
     * from the region-and-hash identity rather than being recomputed from city and postcode.
     * Returns {@code "UNKNOWN"} when the code is absent or carries no region prefix.
     */
    public static String ofCustomerCode(String customerCode) {
        if (customerCode == null) {
            return "UNKNOWN";
        }
        int dash = customerCode.indexOf('-');
        if (dash <= 0) {
            return "UNKNOWN";
        }
        return customerCode.substring(0, dash);
    }

    /** The region whose range contains {@code postcode}, or {@code null} when none does. */
    private static String regionForPostcode(String postcode) {
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
