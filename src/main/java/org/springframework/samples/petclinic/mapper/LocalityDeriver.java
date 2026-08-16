package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's locality (region) from their city using a fixed
 * city-to-region table (Sydney -> NSW, Melbourne -> VIC, Brisbane -> QLD).
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper})
 * so MapStruct does not mistake it for a generic {@code String -> String}
 * mapping method and apply it to unrelated fields; the mapper references it only
 * through an explicit expression.
 */
public final class LocalityDeriver {

    private LocalityDeriver() {
    }

    /**
     * Returns the canonical region string for the given city, or {@code "UNKNOWN"}
     * when the city is {@code null} or not in the fixed table.
     */
    public static String locality(String city) {
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
