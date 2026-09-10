package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's canonical region ('locality') from its {@code customerCode}. Since the redesign of owner
 * identity, the customer code is formatted {@code <REGION>-<HASH8>} (see
 * {@link IdentityKeyResolver#deriveCustomerCode}), so the locality is simply the {@code REGION} segment — the region
 * derived from the owner's postcode — rather than being recomputed from the city and postcode. {@link #UNKNOWN} is
 * returned when the customer code is absent or carries no region segment. Kept out of {@link OwnerMapper} so MapStruct
 * does not mistake it for an implicit {@code String -> String} property mapping method.
 */
public final class LocalityResolver {

    /** Locality returned for an owner whose customer code carries no known {@link Region}. */
    public static final String UNKNOWN = "UNKNOWN";

    private LocalityResolver() {
    }

    /**
     * Returns the canonical region ('locality') for the given {@code customerCode}, which is the {@code REGION} segment
     * of the {@code <REGION>-<HASH8>} customer code (the region derived from the owner's postcode). Returns
     * {@link #UNKNOWN} when the customer code is {@code null}, blank, or carries no region segment.
     *
     * @param customerCode the owner's customer code, may be {@code null}
     * @return the region segment of the customer code, or {@link #UNKNOWN} when none is present
     */
    public static String deriveLocality(String customerCode) {
        if (customerCode == null) {
            return UNKNOWN;
        }
        int dash = customerCode.indexOf('-');
        String region = dash < 0 ? customerCode : customerCode.substring(0, dash);
        return region.isBlank() ? UNKNOWN : region;
    }
}
