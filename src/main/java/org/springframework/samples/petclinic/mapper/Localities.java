package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Derives an owner's canonical region ("locality") from their city using a fixed
 * city-to-region table. Kept as a plain utility (rather than a mapper method) so
 * MapStruct does not treat it as an implicit {@code String -> String} mapping.
 */
public final class Localities {

    /** City -> canonical region. Anything not listed derives {@link #UNKNOWN}. */
    private static final Map<String, String> CITY_REGION = Map.of(
        "Sydney", "NSW",
        "Melbourne", "VIC",
        "Brisbane", "QLD");

    /** Region returned for a city that is absent from {@link #CITY_REGION}. */
    public static final String UNKNOWN = "UNKNOWN";

    private Localities() {
    }

    /**
     * Returns the canonical region for {@code city} from the fixed table
     * ({@code Sydney -> NSW}, {@code Melbourne -> VIC}, {@code Brisbane -> QLD}),
     * or {@code "UNKNOWN"} when the city is {@code null} or not in the table.
     *
     * @param city the owner's city
     * @return the canonical region string, or {@code "UNKNOWN"} when unknown
     */
    public static String forCity(String city) {
        if (city == null) {
            return UNKNOWN;
        }
        return CITY_REGION.getOrDefault(city, UNKNOWN);
    }
}
