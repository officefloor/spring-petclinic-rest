package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code ownerSegment}, formatted {@code "<TIER>_<AREA>"}: one of
 * {@code "PREMIUM_METRO"}, {@code "PREMIUM_REGIONAL"}, {@code "STANDARD_METRO"} or
 * {@code "STANDARD_REGIONAL"}.
 *
 * <p>TIER is {@code "PREMIUM"} when the owner's effective membership level (see
 * {@link MembershipLevels#cappedLevelFor(Owner)}) is 3 or more, otherwise
 * {@code "STANDARD"}. AREA is {@code "METRO"} when the owner's locality (see
 * {@link Localities#localityFor(Owner)}) is a known region (NSW, VIC or QLD), otherwise
 * {@code "REGIONAL"}.
 */
public final class OwnerSegments {

    /** Minimum membership level for the {@code PREMIUM} tier. */
    private static final int PREMIUM_LEVEL = 3;

    private OwnerSegments() {
    }

    /**
     * @param owner the pet owner.
     * @return the owner's segment as {@code "<TIER>_<AREA>"}.
     */
    public static String segmentFor(Owner owner) {
        String tier = MembershipLevels.cappedLevelFor(owner) >= PREMIUM_LEVEL ? "PREMIUM" : "STANDARD";
        String area = Localities.UNKNOWN.equals(Localities.localityFor(owner)) ? "REGIONAL" : "METRO";
        return tier + "_" + area;
    }
}
