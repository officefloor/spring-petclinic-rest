package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's {@code locality} (canonical region). The postcode is preferred: the
 * region is resolved by postcode range first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099)
 * and only falls back to the fixed city-to-region table when the postcode is absent or in
 * no known range. Kept out of {@link OwnerMapper} so MapStruct does not mistake it for a
 * {@code String -> String} mapping method and apply it to every string property.
 */
public final class OwnerLocality {

    /** Fixed city-to-region table. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    /** Region -> inclusive 4-digit postcode range {low, high}. */
    private static final Map<String, int[]> REGION_POSTCODES = Map.of(
        "NSW", new int[] {2000, 2099}, "VIC", new int[] {3000, 3099}, "QLD", new int[] {4000, 4099});

    private OwnerLocality() {
    }

    /**
     * Return the canonical region for {@code city}, or {@code "UNKNOWN"} when the city is
     * null or not present in the table.
     */
    public static String forCity(String city) {
        return city == null ? "UNKNOWN" : CITY_REGION.getOrDefault(city, "UNKNOWN");
    }

    /**
     * Return the canonical region, preferring the {@code postcode}: if it is a 4-digit code
     * that falls inside a known region's range, that region is returned; otherwise fall back
     * to the city-to-region table. Returns {@code "UNKNOWN"} when neither resolves a region.
     */
    public static String forCityAndPostcode(String city, String postcode) {
        String byPostcode = forPostcode(postcode);
        return byPostcode != null ? byPostcode : forCity(city);
    }

    /**
     * Return the canonical region derived from the {@code postcode} alone (NSW 2000-2099,
     * VIC 3000-3099, QLD 4000-4099), or {@code "UNKNOWN"} when the postcode is absent,
     * malformed, or in no known range. This is the REGION component of an owner's
     * {@code customerCode}.
     */
    public static String forPostcodeOrUnknown(String postcode) {
        String region = forPostcode(postcode);
        return region != null ? region : "UNKNOWN";
    }

    /**
     * Return the REGION component of a {@code customerCode} formatted {@code '<REGION>-<HASH8>'},
     * i.e. the text before the first {@code '-'}. Returns {@code "UNKNOWN"} when the customer
     * code is null or carries no region prefix. This is the owner's derived {@code locality}.
     */
    public static String regionFromCustomerCode(String customerCode) {
        if (customerCode == null) {
            return "UNKNOWN";
        }
        int dash = customerCode.indexOf('-');
        return dash > 0 ? customerCode.substring(0, dash) : "UNKNOWN";
    }

    /**
     * Compute the single Luhn check digit (0-9) over the digits contained in {@code s}.
     * Non-digit characters are ignored; the rightmost digit is doubled first. Kept here,
     * alongside the other derived-field helpers, so MapStruct does not treat it as an
     * implicit mapping method.
     */
    public static int luhn(String s) {
        int sum = 0;
        boolean dbl = true;
        for (int i = s.length() - 1; i >= 0; i--) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * Return the canonical region whose postcode range contains {@code postcode}, or
     * {@code null} when the postcode is absent, malformed, or in no known range.
     */
    private static String forPostcode(String postcode) {
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
