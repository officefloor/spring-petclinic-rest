package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's region, either from a postcode or from the region-and-hash
 * {@code memberId}, plus the legacy city-to-region lookup.
 *
 * <p>{@link #region(String)} maps a postcode to its region by the fixed ranges
 * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099); this is the region embedded in
 * the {@code <REGION>} prefix of an owner's {@code '<REGION><FY><HASH8><CHK>'}
 * memberId. {@link #locality(String)} reads an owner's locality straight back out
 * of that memberId (its {@code <REGION>} prefix), so the locality and the identity
 * always agree. The city-to-region table (Sydney -> NSW, Melbourne -> VIC,
 * Brisbane -> QLD) remains available via {@link #localityForCity(String)} for
 * postcode validation.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper})
 * so MapStruct does not mistake it for a generic {@code String -> String}
 * mapping method and apply it to unrelated fields; the mapper references it only
 * through an explicit expression.
 */
public final class LocalityDeriver {

    /** The known region prefixes carried at the front of a memberId, longest first. */
    private static final String[] KNOWN_REGIONS = {"NSW", "VIC", "QLD"};

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
     * Returns an owner's locality: the {@code <REGION>} prefix of the {@code '<REGION><FY><HASH8><CHK>'}
     * memberId. Recognises the known region prefixes {@code NSW}/{@code VIC}/{@code QLD}; anything else
     * (including a memberId built with an {@code UNKNOWN} region) yields {@code "UNKNOWN"}. Returns
     * {@code "UNKNOWN"} when the memberId is {@code null}/blank.
     */
    public static String locality(String memberId) {
        if (memberId == null || memberId.isBlank()) {
            return "UNKNOWN";
        }
        for (String region : KNOWN_REGIONS) {
            if (memberId.startsWith(region)) {
                return region;
            }
        }
        return "UNKNOWN";
    }

    /**
     * Returns the IANA timezone name for the given locality/region via the fixed
     * region-to-timezone table (NSW -> {@code Australia/Sydney},
     * VIC -> {@code Australia/Melbourne}, QLD -> {@code Australia/Brisbane}), or
     * {@code "UNKNOWN"} when the region is {@code null} or not in the table.
     */
    public static String timezone(String region) {
        if (region == null) {
            return "UNKNOWN";
        }
        return switch (region) {
            case "NSW" -> "Australia/Sydney";
            case "VIC" -> "Australia/Melbourne";
            case "QLD" -> "Australia/Brisbane";
            default -> "UNKNOWN";
        };
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
