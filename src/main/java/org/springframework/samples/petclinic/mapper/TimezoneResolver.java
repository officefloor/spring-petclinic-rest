package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's IANA {@code timezone} from its locality/region via the fixed region-to-timezone table
 * (NSW -> {@code Australia/Sydney}, VIC -> {@code Australia/Melbourne}, QLD -> {@code Australia/Brisbane}). The
 * locality is the region name resolved by {@link LocalityResolver#deriveLocality} from the owner's member id, so
 * this resolver simply looks that region up in {@link Region} and returns its timezone. {@code null} is returned when
 * the locality carries no known region (including {@link LocalityResolver#UNKNOWN}), which leaves the field absent.
 * Kept out of {@link OwnerMapper} so MapStruct does not mistake it for an implicit {@code String -> String} property
 * mapping method.
 */
public final class TimezoneResolver {

    private TimezoneResolver() {
    }

    /**
     * Returns the IANA timezone name for the given {@code memberId}, derived from the owner's locality/region.
     * Returns {@code null} when the member id carries no known region.
     *
     * @param memberId the owner's member id, may be {@code null}
     * @return the region's IANA timezone name, or {@code null} when the locality maps to no known region
     */
    public static String deriveTimezone(String memberId) {
        String locality = LocalityResolver.deriveLocality(memberId);
        for (Region region : Region.values()) {
            if (region.name().equals(locality)) {
                return region.getTimezone();
            }
        }
        return null;
    }
}
