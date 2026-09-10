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

    NSW("Sydney", 2000, 2099),
    VIC("Melbourne", 3000, 3099),
    QLD("Brisbane", 4000, 4099);

    private final String city;

    private final int minPostcode;

    private final int maxPostcode;

    Region(String city, int minPostcode, int maxPostcode) {
        this.city = city;
        this.minPostcode = minPostcode;
        this.maxPostcode = maxPostcode;
    }

    /**
     * Returns whether the given (already 4-digit) postcode falls within this region's inclusive
     * postcode range (NSW 2000-2099, VIC 3000-3099, QLD 4000-4099).
     *
     * @param postcode the numeric postcode value to test
     * @return {@code true} when the postcode is within the region's range, {@code false} otherwise
     */
    public boolean acceptsPostcode(int postcode) {
        return postcode >= this.minPostcode && postcode <= this.maxPostcode;
    }

    /**
     * Returns the region whose inclusive postcode range contains the given postcode, or
     * {@code null} when the postcode is absent, not a 4-digit number, or in no known range.
     *
     * @param postcode the owner's postcode, may be {@code null}
     * @return the matching region, or {@code null} when the postcode maps to no known region
     */
    public static Region forPostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        int value;
        try {
            value = Integer.parseInt(postcode.trim());
        }
        catch (NumberFormatException ex) {
            return null;
        }
        for (Region region : values()) {
            if (region.acceptsPostcode(value)) {
                return region;
            }
        }
        return null;
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
