package org.springframework.samples.petclinic.util;

import java.util.Set;

/**
 * Derives an owner's {@code ownerSegment}, formatted {@code <TIER>_<AREA>}. TIER is
 * {@code PREMIUM} once the membership level reaches 3, otherwise {@code STANDARD}. AREA is
 * {@code METRO} when the locality is a known metropolitan region (NSW, VIC or QLD),
 * otherwise {@code REGIONAL}.
 */
public final class OwnerSegment {

    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    /** The {@code <TIER>_<AREA>} segment for a membership level and locality. */
    public static String of(int membershipLevel, String locality) {
        String tier = membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
