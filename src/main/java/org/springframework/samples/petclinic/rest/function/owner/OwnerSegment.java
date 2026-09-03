package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

/**
 * Derives an owner's segment '<TIER>_<AREA>'. TIER is 'PREMIUM' when membershipLevel is 3 or more,
 * otherwise 'STANDARD'. AREA is 'METRO' when the locality is a known region (NSW, VIC or QLD),
 * otherwise 'REGIONAL'.
 */
public final class OwnerSegment {

    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    public static String of(Integer membershipLevel, String region) {
        String tier = membershipLevel != null && membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(region) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
