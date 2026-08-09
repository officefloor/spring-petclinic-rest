package org.springframework.samples.petclinic.mapper;

import java.util.Set;

/**
 * Derives an owner's {@code ownerSegment}, the marketing segment formatted
 * {@code '<TIER>_<AREA>'}. TIER is {@code "PREMIUM"} when the owner's membershipLevel is 3 or
 * more, otherwise {@code "STANDARD"}. AREA is {@code "METRO"} when the owner's locality is a
 * known region (NSW, VIC or QLD), otherwise {@code "REGIONAL"}. The four possible values are
 * therefore {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} and
 * {@code STANDARD_REGIONAL}.
 *
 * <p>Kept out of {@link OwnerMapper} so MapStruct does not mistake it for an implicit mapping
 * method and apply it to every property. Because it depends on the membershipLevel (populated
 * by the controller), the controller sets {@code ownerSegment} once the level is known.
 */
public final class OwnerSegment {

    /** The localities (regions) that count as METRO; anything else is REGIONAL. */
    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    /**
     * Compute the owner's segment from its membership level and locality.
     *
     * @param membershipLevel the owner's numeric membership level (1-4)
     * @param locality        the owner's locality (canonical region, or {@code "UNKNOWN"})
     * @return the segment formatted {@code '<TIER>_<AREA>'}
     */
    public static String segment(int membershipLevel, String locality) {
        String tier = membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = locality != null && METRO_REGIONS.contains(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
