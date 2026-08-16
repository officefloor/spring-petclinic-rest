package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Derives an owner's region code — the leading {@code <REGION>} segment of the unified
 * {@code memberId} and the source of the owner's locality.
 *
 * <p>The postcode is preferred: a four-digit postcode is mapped by inclusive range
 * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). When the postcode is absent or in no
 * known range, falls back to the fixed city-to-region table (Sydney-&gt;NSW,
 * Melbourne-&gt;VIC, Brisbane-&gt;QLD), otherwise {@code "UNKNOWN"}.
 */
public final class OwnerRegion {

    private OwnerRegion() {
    }

    /**
     * The version-2 region code used <em>inside the identifiers</em> — the plain
     * {@link #fromPostcodeOrCity region} with the fixed {@link OwnerIdentityVersion#TAG "V2"} tag
     * prefixed (e.g. {@code "V2NSW"}). This is the region embedded in the {@code memberId}; the
     * user-facing {@code locality}, {@code timezone} and owner segment keep the plain region.
     */
    public static String identityRegion(String postcode, String city) {
        return OwnerIdentityVersion.TAG + fromPostcodeOrCity(postcode, city);
    }

    /** Region derived from the postcode, falling back to the city, else {@code "UNKNOWN"}. */
    public static String fromPostcodeOrCity(String postcode, String city) {
        if (postcode != null && postcode.matches("[0-9]{4}")) {
            int value = Integer.parseInt(postcode);
            if (value >= 2000 && value <= 2099) return "NSW";
            if (value >= 3000 && value <= 3099) return "VIC";
            if (value >= 4000 && value <= 4099) return "QLD";
        }
        if ("Sydney".equals(city)) return "NSW";
        if ("Melbourne".equals(city)) return "VIC";
        if ("Brisbane".equals(city)) return "QLD";
        return "UNKNOWN";
    }
}
