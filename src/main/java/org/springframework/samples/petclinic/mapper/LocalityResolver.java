package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's canonical region ('locality') from its city. The city-to-region
 * table lives on {@link Region}; this resolver turns the resolved region into the
 * {@code locality} string an owner is mapped with, using {@link #UNKNOWN} for a city
 * that belongs to no known region. Kept out of {@link OwnerMapper} so MapStruct does not
 * mistake it for an implicit {@code String -> String} property mapping method.
 */
public final class LocalityResolver {

    /** Locality returned for a city that belongs to no known {@link Region}. */
    public static final String UNKNOWN = "UNKNOWN";

    private LocalityResolver() {
    }

    /**
     * Returns the canonical region for the given city (Sydney-&gt;NSW,
     * Melbourne-&gt;VIC, Brisbane-&gt;QLD), or "UNKNOWN" when the city is not in
     * the table (including a {@code null} city).
     */
    public static String deriveLocality(String city) {
        Region region = Region.forCity(city);
        return region == null ? UNKNOWN : region.name();
    }
}
