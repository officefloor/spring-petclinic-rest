package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * The single definition of an owner's locality: the canonical region derived from its
 * postcode range first (see {@link Postcode}), falling back to a fixed city-to-region table
 * when the postcode is absent or in no known range. Owners matching neither resolve to
 * {@code "UNKNOWN"}.
 *
 * <p>Lookup only; a pure function of the postcode and city, so the region is computed on read
 * rather than stored.
 */
public final class Locality {

    /** The unknown-region marker returned for any city not in {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    /** City -> canonical region. */
    private static final Map<String, String> CITY_REGION =
            Map.of("Sydney", "NSW", "Melbourne", "VIC", "Brisbane", "QLD");

    private Locality() {
    }

    /** The canonical region for {@code city} via the city-to-region table alone, or
     *  {@code "UNKNOWN"} when it is not in the table. */
    public static String region(String city) {
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }

    /** The canonical region for an owner: the postcode's region when its range identifies one,
     *  otherwise the city's region, or {@code "UNKNOWN"} when neither applies. */
    public static String region(String city, String postcode) {
        String byPostcode = Postcode.region(postcode);
        return byPostcode != null ? byPostcode : region(city);
    }
}
