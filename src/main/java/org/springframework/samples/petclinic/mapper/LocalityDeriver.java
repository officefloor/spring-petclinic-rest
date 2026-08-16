package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's locality (region) preferring the postcode over the city.
 *
 * <p>The postcode range is consulted first (NSW 2000-2099, VIC 3000-3099,
 * QLD 4000-4099); only when the postcode is absent or falls in no known range
 * does derivation fall back to the fixed city-to-region table (Sydney -> NSW,
 * Melbourne -> VIC, Brisbane -> QLD). This returns the same region for the known
 * cities but disambiguates cities that share a name.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper})
 * so MapStruct does not mistake it for a generic {@code String -> String}
 * mapping method and apply it to unrelated fields; the mapper references it only
 * through an explicit expression.
 */
public final class LocalityDeriver {

    private LocalityDeriver() {
    }

    /**
     * Returns the canonical region string, preferring the postcode: if the postcode falls in a
     * known region's range it wins; otherwise the city-to-region table is used. Returns
     * {@code "UNKNOWN"} when neither yields a region.
     */
    public static String locality(String city, String postcode) {
        String byPostcode = regionForPostcode(postcode);
        if (byPostcode != null) {
            return byPostcode;
        }
        return locality(city);
    }

    /**
     * Returns the canonical region string for the given city, or {@code "UNKNOWN"}
     * when the city is {@code null} or not in the fixed table.
     */
    public static String locality(String city) {
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
     * Returns the region whose fixed inclusive range contains the given 4-digit postcode
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), or {@code null} when the postcode is
     * absent, not a 4-digit value, or in no known range.
     */
    private static String regionForPostcode(String postcode) {
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
