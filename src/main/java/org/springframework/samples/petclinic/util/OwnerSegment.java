package org.springframework.samples.petclinic.util;

/**
 * Derives an owner's market segment '&lt;TIER&gt;_&lt;AREA&gt;' from their membership level and
 * locality. TIER is PREMIUM at membership level {@link #PREMIUM_LEVEL} or above, otherwise
 * STANDARD. AREA is METRO for a known region (a resolved locality), otherwise REGIONAL.
 */
public enum OwnerSegment {

    PREMIUM_METRO, PREMIUM_REGIONAL, STANDARD_METRO, STANDARD_REGIONAL;

    /** Membership level (inclusive) at and above which an owner is PREMIUM. */
    public static final int PREMIUM_LEVEL = 3;

    /**
     * Return the segment for an owner with the given {@code membershipLevel} and {@code locality}
     * (a canonical region, or {@link CityRegions#UNKNOWN} when the region is not known).
     */
    public static OwnerSegment of(Integer membershipLevel, String locality) {
        boolean premium = membershipLevel != null && membershipLevel >= PREMIUM_LEVEL;
        boolean metro = !CityRegions.UNKNOWN.equals(locality);
        if (premium) {
            return metro ? PREMIUM_METRO : PREMIUM_REGIONAL;
        }
        return metro ? STANDARD_METRO : STANDARD_REGIONAL;
    }
}
