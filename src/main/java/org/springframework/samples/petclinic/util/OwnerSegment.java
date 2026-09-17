package org.springframework.samples.petclinic.util;

/**
 * An owner's segment, formatted {@code <TIER>_<AREA>}. The tier is {@code PREMIUM} when the owner's
 * membership level (see {@link MembershipLevel}) is 3 or more, otherwise {@code STANDARD}. The area
 * is {@code METRO} when the owner's locality is a known region (see {@link CityRegion#isKnown(String)}),
 * otherwise {@code REGIONAL}. Composed at response time from the already-derived membership level and
 * locality, so it is derived rather than stored on the entity.
 */
public enum OwnerSegment {

    PREMIUM_METRO,

    PREMIUM_REGIONAL,

    STANDARD_METRO,

    STANDARD_REGIONAL;

    /** The smallest membership level that counts as the {@code PREMIUM} tier. */
    private static final int PREMIUM_LEVEL = 3;

    /**
     * The segment for the given {@code membershipLevel} (see {@link MembershipLevel}) and
     * {@code locality} (a region, see {@link CityRegion}). A {@code null} membership level is treated
     * as below the premium threshold.
     */
    public static OwnerSegment of(Integer membershipLevel, String locality) {
        boolean premium = membershipLevel != null && membershipLevel >= PREMIUM_LEVEL;
        boolean metro = CityRegion.isKnown(locality);
        if (premium) {
            return metro ? PREMIUM_METRO : PREMIUM_REGIONAL;
        }
        return metro ? STANDARD_METRO : STANDARD_REGIONAL;
    }
}
