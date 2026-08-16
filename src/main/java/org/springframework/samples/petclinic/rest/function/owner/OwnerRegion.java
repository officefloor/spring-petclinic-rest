package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Derives an owner's region code — the {@code <REGION>} segment of the
 * {@code <REGION>-<HASH8>} {@code customerCode} and the source of the owner's locality.
 *
 * <p>The postcode is preferred: a four-digit postcode is mapped by inclusive range
 * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099). When the postcode is absent or in no
 * known range, falls back to the fixed city-to-region table (Sydney-&gt;NSW,
 * Melbourne-&gt;VIC, Brisbane-&gt;QLD), otherwise {@code "UNKNOWN"}.
 */
public final class OwnerRegion {

    private OwnerRegion() {
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
