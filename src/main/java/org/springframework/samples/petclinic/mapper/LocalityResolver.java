package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's canonical region ('locality'), preferring the postcode over the city.
 * Both the postcode-range and city-to-region tables live on {@link Region}; this resolver
 * looks the region up by postcode first and only falls back to the city when the postcode is
 * absent or in no known range, turning the resolved region into the {@code locality} string an
 * owner is mapped with, using {@link #UNKNOWN} when neither postcode nor city maps to a known
 * region. Kept out of {@link OwnerMapper} so MapStruct does not mistake it for an implicit
 * {@code String -> String} property mapping method.
 */
public final class LocalityResolver {

    /** Locality returned for a city that belongs to no known {@link Region}. */
    public static final String UNKNOWN = "UNKNOWN";

    private LocalityResolver() {
    }

    /**
     * Returns the canonical region for the given owner, preferring the postcode range
     * (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099) over the city-to-region table
     * (Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD). Falls back to the city only when the
     * postcode is absent or in no known range, and returns "UNKNOWN" when neither maps to a
     * known region (including a {@code null} city and postcode).
     */
    public static String deriveLocality(String city, String postcode) {
        Region region = Region.forPostcode(postcode);
        if (region == null) {
            region = Region.forCity(city);
        }
        return region == null ? UNKNOWN : region.name();
    }
}
