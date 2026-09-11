package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's marketing 'ownerSegment', formatted {@code <TIER>_<AREA>}. TIER is
 * {@code PREMIUM} when the owner's {@link MembershipLevelResolver#deriveMembershipLevel(Owner)
 * membershipLevel} is 3 or more, otherwise {@code STANDARD}. AREA is {@code METRO} when the owner's
 * {@link LocalityResolver#deriveLocality(Owner) locality} is a known {@link Region} (NSW, VIC or
 * QLD), otherwise {@code REGIONAL}. The result is one of {@code PREMIUM_METRO},
 * {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or {@code STANDARD_REGIONAL}. Kept out of
 * {@link OwnerMapper} so MapStruct does not mistake it for an implicit property mapping method.
 */
public final class OwnerSegmentResolver {

    /** Membership level at or above which an owner is in the {@code PREMIUM} tier. */
    private static final int PREMIUM_MIN_LEVEL = 3;

    private OwnerSegmentResolver() {
    }

    /**
     * Returns the marketing segment for an owner, formatted {@code <TIER>_<AREA>} (see
     * {@link OwnerSegmentResolver class docs}).
     *
     * @param owner the owner whose segment should be derived
     * @return the owner's segment, one of {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL},
     * {@code STANDARD_METRO} or {@code STANDARD_REGIONAL}
     */
    public static String deriveOwnerSegment(Owner owner) {
        String tier = MembershipLevelResolver.deriveMembershipLevel(owner) >= PREMIUM_MIN_LEVEL
            ? "PREMIUM" : "STANDARD";
        String locality = LocalityResolver.deriveLocality(owner);
        String area = isKnownRegion(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * Reports whether the given locality names a known {@link Region} (NSW, VIC or QLD).
     *
     * @param locality the owner's locality, may be the {@link LocalityResolver#UNKNOWN} sentinel
     * @return {@code true} when the locality is a known region, {@code false} otherwise
     */
    private static boolean isKnownRegion(String locality) {
        for (Region region : Region.values()) {
            if (region.name().equals(locality)) {
                return true;
            }
        }
        return false;
    }
}
