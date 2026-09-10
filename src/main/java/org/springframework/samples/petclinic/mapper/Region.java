package org.springframework.samples.petclinic.mapper;

/**
 * The canonical regions an owner's city can belong to. Modeling a region as a first-class type
 * (rather than a bare string) gives the region-keyed rules a single home: each region owns the
 * cities that resolve to it, so further region-scoped facts can hang off the same constant instead
 * of being duplicated as a parallel city switch.
 *
 * <p>Kept in the {@code mapper} package alongside {@link LocalityResolver}, which turns a region
 * into the {@code locality} string an owner is mapped with. Not a {@code @Mapper} type, so MapStruct
 * ignores it.
 */
public enum Region {

    NSW("Sydney"),
    VIC("Melbourne"),
    QLD("Brisbane");

    private final String city;

    Region(String city) {
        this.city = city;
    }

    /**
     * Returns the region the given city belongs to, or {@code null} when the city is not in the
     * table (including a {@code null} city).
     *
     * @param city the owner's city, may be {@code null}
     * @return the matching region, or {@code null} when the city belongs to no known region
     */
    public static Region forCity(String city) {
        if (city == null) {
            return null;
        }
        for (Region region : values()) {
            if (region.city.equals(city)) {
                return region;
            }
        }
        return null;
    }
}
