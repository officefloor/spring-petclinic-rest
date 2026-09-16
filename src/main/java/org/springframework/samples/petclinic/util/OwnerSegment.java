package org.springframework.samples.petclinic.util;

/**
 * The owner's marketing segment, formatted {@code <TIER>_<AREA>}: the membership tier and
 * the region area combined into one of {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL},
 * {@code STANDARD_METRO} or {@code STANDARD_REGIONAL}.
 *
 * <p>TIER is {@code PREMIUM} at membership level 3 or above (see {@link MembershipLevel}),
 * otherwise {@code STANDARD}. AREA is {@code METRO} when the owner's locality is a known
 * region — any region other than {@link CityRegion#UNKNOWN}, i.e. NSW, VIC or QLD (see
 * {@link OwnerRegion}) — otherwise {@code REGIONAL}.
 */
public final class OwnerSegment {

    /** Lowest membership level whose tier is {@code PREMIUM}. */
    private static final int PREMIUM_LEVEL = 3;

    private OwnerSegment() {
    }

    /**
     * The segment for the given membership level and locality.
     *
     * @param membershipLevel the owner's membership level (see {@link MembershipLevel})
     * @param locality        the owner's locality (see {@link OwnerRegion})
     * @return the {@code <TIER>_<AREA>} segment
     */
    public static String of(int membershipLevel, String locality) {
        return tier(membershipLevel) + "_" + area(locality);
    }

    private static String tier(int membershipLevel) {
        return membershipLevel >= PREMIUM_LEVEL ? "PREMIUM" : "STANDARD";
    }

    private static String area(String locality) {
        return CityRegion.UNKNOWN.equals(locality) ? "REGIONAL" : "METRO";
    }
}
