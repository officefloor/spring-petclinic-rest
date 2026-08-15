package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's locality (region) from its postcode and city.
 *
 * <p>The postcode takes precedence: the region is resolved from the postcode range first
 * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), and only when the postcode is absent or falls in
 * no known range does derivation fall back to the fixed city-to-region table. This returns the same
 * region for the pinned cities but disambiguates cities that share a name.
 *
 * <p>Kept as a standalone helper (rather than a mapper method) so MapStruct does not adopt it
 * as an implicit {@code String -> String} mapping method for unrelated owner string properties.
 */
public final class OwnerLocality {

    private OwnerLocality() {
    }

    /**
     * Derives an owner's canonical region, preferring the postcode over the city. The region is
     * resolved from the postcode range first (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099); when the
     * postcode is {@code null} or lies in no known range, derivation falls back to the city-to-region
     * table via {@link #of(String)}.
     *
     * @param city the owner's city (may be {@code null})
     * @param postcode the owner's postcode (may be {@code null})
     * @return the canonical region string, or {@code "UNKNOWN"} when neither the postcode nor the
     *         city resolves to a known region
     */
    public static String of(String city, String postcode) {
        String byPostcode = fromPostcode(postcode);
        return byPostcode != null ? byPostcode : of(city);
    }

    /**
     * Maps a city to its canonical region: Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD.
     *
     * @param city the owner's city (may be {@code null})
     * @return the canonical region string, or {@code "UNKNOWN"} when the city is not in the table
     */
    public static String of(String city) {
        if (city == null) {
            return "UNKNOWN";
        }
        return switch (city) {
            case "Sydney" -> "NSW";
            case "Melbourne" -> "VIC";
            case "Brisbane" -> "QLD";
            default -> "UNKNOWN";
        };
    }

    /**
     * Derives an owner's IANA timezone from its locality (region), preferring the postcode over the
     * city, via the fixed region-to-timezone table: NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne,
     * QLD-&gt;Australia/Brisbane. The region is resolved with {@link #of(String, String)}.
     *
     * @param city the owner's city (may be {@code null})
     * @param postcode the owner's postcode (may be {@code null})
     * @return the IANA timezone name, or {@code null} when the region is not in the table
     */
    public static String timezoneOf(String city, String postcode) {
        return timezoneOfRegion(of(city, postcode));
    }

    /**
     * Maps a canonical region to its IANA timezone: NSW-&gt;Australia/Sydney, VIC-&gt;Australia/Melbourne,
     * QLD-&gt;Australia/Brisbane.
     *
     * @param region the canonical region string (may be {@code null})
     * @return the IANA timezone name, or {@code null} when the region is not in the table
     */
    public static String timezoneOfRegion(String region) {
        if (region == null) {
            return null;
        }
        return switch (region) {
            case "NSW" -> "Australia/Sydney";
            case "VIC" -> "Australia/Melbourne";
            case "QLD" -> "Australia/Brisbane";
            default -> null;
        };
    }

    /**
     * Resolves a region from a postcode range: NSW 2000-2099, VIC 3000-3099, QLD 4000-4099.
     *
     * @param postcode the owner's postcode (may be {@code null})
     * @return the region string, or {@code null} when the postcode is absent, not a 4-digit value, or
     *         falls in no known range
     */
    private static String fromPostcode(String postcode) {
        if (postcode == null || !postcode.matches("[0-9]{4}")) {
            return null;
        }
        int value = Integer.parseInt(postcode);
        if (value >= 2000 && value <= 2099) {
            return "NSW";
        }
        if (value >= 3000 && value <= 3099) {
            return "VIC";
        }
        if (value >= 4000 && value <= 4099) {
            return "QLD";
        }
        return null;
    }
}
