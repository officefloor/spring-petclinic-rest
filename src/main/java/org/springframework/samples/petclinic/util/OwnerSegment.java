package org.springframework.samples.petclinic.util;

/**
 * Derives an owner's marketing segment '&lt;TIER&gt;_&lt;AREA&gt;'. TIER is 'PREMIUM' when the
 * owner's membership level is 3 or more, otherwise 'STANDARD'. AREA is 'METRO' when the owner's
 * {@link OwnerRegion region} is a {@link CityRegion#isKnown known region} (NSW, VIC or QLD),
 * otherwise 'REGIONAL'.
 */
public final class OwnerSegment {

    private OwnerSegment() {
    }

    /**
     * The segment for an owner with the given {@code membershipLevel} (null counts as below the
     * premium threshold) and canonical {@code region}.
     */
    public static String of(Integer membershipLevel, String region) {
        String tier = membershipLevel != null && membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = CityRegion.isKnown(region) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
