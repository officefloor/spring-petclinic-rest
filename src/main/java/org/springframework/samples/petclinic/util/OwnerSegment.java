package org.springframework.samples.petclinic.util;

import java.util.Set;

/**
 * Derives an owner's {@code ownerSegment}, formatted {@code '<TIER>_<AREA>'}. TIER is
 * {@code PREMIUM} when the membership level is 3 or more, otherwise {@code STANDARD}. AREA is
 * {@code METRO} when the locality is a known region (NSW, VIC or QLD), otherwise {@code REGIONAL}.
 * The four possible segments are {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL},
 * {@code STANDARD_METRO} and {@code STANDARD_REGIONAL}.
 */
public final class OwnerSegment {

    /** The regions treated as METRO. */
    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    /** The {@code '<TIER>_<AREA>'} segment for the given {@code membershipLevel} and {@code locality}. */
    public static String of(int membershipLevel, String locality) {
        String tier = membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
