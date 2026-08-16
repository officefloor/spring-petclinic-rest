package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's region, either the plain user-facing region from a postcode or
 * the version-2 region embedded inside the identifiers, plus the legacy
 * city-to-region lookup.
 *
 * <p>{@link #region(String)} maps a postcode to its plain region by the fixed ranges
 * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). This is the value returned by the
 * user-facing {@code locality} field, and it never carries the version tag.
 * {@link #identifierRegion(String)} is the version-2 region embedded INSIDE an owner's
 * {@code '<REGION><FY><HASH8><CHK>'} memberId: the fixed {@code 'V2'} version tag
 * followed by the plain region (e.g. {@code 'V2NSW'}), so the identifier changes while
 * the locality stays plain. {@link #embeddedRegion(String)} reads that version-2 region
 * prefix back out of a memberId, used to locate the segments that follow it. The
 * city-to-region table (Sydney -> NSW, Melbourne -> VIC, Brisbane -> QLD) remains
 * available via {@link #localityForCity(String)} for postcode validation.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper})
 * so MapStruct does not mistake it for a generic {@code String -> String}
 * mapping method and apply it to unrelated fields; the mapper references it only
 * through an explicit expression.
 */
public final class LocalityDeriver {

    /**
     * The fixed version tag mixed into the region code used inside the identifiers, so
     * a version-2 identifier never reproduces the value version 1 would have produced.
     */
    public static final String VERSION_TAG = "V2";

    /** The known plain region codes, longest first. */
    private static final String[] KNOWN_REGIONS = {"NSW", "VIC", "QLD"};

    private LocalityDeriver() {
    }

    /**
     * Returns the plain region code derived from the postcode: the region whose fixed inclusive
     * range contains the 4-digit postcode (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099), or
     * {@code "UNKNOWN"} when the postcode is absent, not a 4-digit value, or in no known range.
     * This is the owner's user-facing locality; it never carries the version tag.
     */
    public static String region(String postcode) {
        String byPostcode = regionForPostcode(postcode);
        return byPostcode != null ? byPostcode : "UNKNOWN";
    }

    /**
     * Returns the version-2 region code embedded INSIDE a new owner's memberId: the fixed
     * {@code 'V2'} version tag concatenated with the plain region derived from the postcode
     * (e.g. {@code 'V2NSW'}, {@code 'V2UNKNOWN'}). The tag guarantees the identifier differs
     * from the value version 1 would have embedded, while the plain {@link #region(String)}
     * stays available for the user-facing locality.
     */
    public static String identifierRegion(String postcode) {
        return VERSION_TAG + region(postcode);
    }

    /**
     * Returns the version-2 region prefix embedded at the front of a
     * {@code '<REGION><FY><HASH8><CHK>'} memberId (e.g. {@code 'V2NSW'}, {@code 'V2UNKNOWN'}),
     * so its length locates the FY segment that follows. Recognises the version-tagged known
     * regions; anything else (including a memberId built with an {@code UNKNOWN} region) yields
     * {@code 'V2UNKNOWN'}. Returns the empty string when the memberId is {@code null}/blank.
     */
    public static String embeddedRegion(String memberId) {
        if (memberId == null || memberId.isBlank()) {
            return "";
        }
        String rest = memberId.startsWith(VERSION_TAG)
            ? memberId.substring(VERSION_TAG.length())
            : memberId;
        for (String region : KNOWN_REGIONS) {
            if (rest.startsWith(region)) {
                return VERSION_TAG + region;
            }
        }
        return VERSION_TAG + "UNKNOWN";
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
