package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's locality (region) from the city using a fixed city-to-region table.
 *
 * <p>Kept as a standalone helper rather than a method on {@link OwnerMapper}: a single-argument
 * {@code String}-to-{@code String} method declared on a MapStruct mapper would be picked up as an
 * implicit conversion and applied to every String property mapping.
 */
final class OwnerLocality {

    private OwnerLocality() {
    }

    /**
     * Returns the canonical region for {@code city} (Sydney-&gt;NSW, Melbourne-&gt;VIC,
     * Brisbane-&gt;QLD), or {@code "UNKNOWN"} when the city is not in the table.
     */
    static String derive(String city) {
        if (city == null) {
            return "UNKNOWN";
        }
        switch (city) {
            case "Sydney":
                return "NSW";
            case "Melbourne":
                return "VIC";
            case "Brisbane":
                return "QLD";
            default:
                return "UNKNOWN";
        }
    }
}
