package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's locality (region) from its city using a fixed city-to-region table.
 *
 * <p>Kept as a standalone helper (rather than a mapper method) so MapStruct does not adopt it
 * as an implicit {@code String -> String} mapping method for unrelated owner string properties.
 */
public final class OwnerLocality {

    private OwnerLocality() {
    }

    /**
     * Maps a city to its canonical region: Sydney-&gt;NSW, Melbourne-&gt;VIC, Brisbane-&gt;QLD.
     *
     * @param city the owner's city (may be {@code null})
     * @return the canonical region string, or {@code "UNKNOWN"} when the city is not in the table
     */
    public static String of(String city) {
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
}
