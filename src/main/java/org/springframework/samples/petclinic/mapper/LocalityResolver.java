package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's canonical region ('locality') from its city using a fixed
 * city-to-region table. Kept out of {@link OwnerMapper} so MapStruct does not
 * mistake it for an implicit {@code String -> String} property mapping method.
 */
public final class LocalityResolver {

    private LocalityResolver() {
    }

    /**
     * Returns the canonical region for the given city (Sydney-&gt;NSW,
     * Melbourne-&gt;VIC, Brisbane-&gt;QLD), or "UNKNOWN" when the city is not in
     * the table (including a {@code null} city).
     */
    public static String deriveLocality(String city) {
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
