package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's region, either from a postcode or from the region-and-hash
 * {@code customerCode}, plus the legacy city-to-region lookup.
 *
 * <p>{@link #region(String)} maps a postcode to its region by the fixed ranges
 * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099); this is the region embedded in
 * an owner's {@code '<REGION>-<HASH8>'} customer code. {@link #locality(String)}
 * reads an owner's locality straight back out of that customer code (its
 * {@code <REGION>} component), so the locality and the identity always agree.
 * The city-to-region table (Sydney -> NSW, Melbourne -> VIC, Brisbane -> QLD)
 * remains available via {@link #localityForCity(String)} for postcode validation.
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
     * Returns the region code derived from the postcode: the region whose fixed inclusive range
     * contains the 4-digit postcode (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), or
     * {@code "UNKNOWN"} when the postcode is absent, not a 4-digit value, or in no known range.
     * This is the {@code <REGION>} component embedded in a new owner's customer code.
     */
    public static String region(String postcode) {
        String byPostcode = regionForPostcode(postcode);
        return byPostcode != null ? byPostcode : "UNKNOWN";
    }

    /**
     * Returns an owner's locality: the {@code <REGION>} component of the {@code '<REGION>-<HASH8>'}
     * customer code (the text before the first {@code '-'}). Returns {@code "UNKNOWN"} when the
     * customer code is {@code null}/blank or carries no region component.
     */
    public static String locality(String customerCode) {
        if (customerCode == null || customerCode.isBlank()) {
            return "UNKNOWN";
        }
        int dash = customerCode.indexOf('-');
        String region = dash < 0 ? customerCode : customerCode.substring(0, dash);
        return region.isBlank() ? "UNKNOWN" : region;
    }

    /**
     * Returns the canonical region string for the given city, or {@code "UNKNOWN"}
     * when the city is {@code null} or not in the fixed table.
     */
    public static String localityForCity(String city) {
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
