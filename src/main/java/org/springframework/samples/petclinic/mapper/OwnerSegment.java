package org.springframework.samples.petclinic.mapper;

import java.util.Set;

/** Derives an owner's segment '<TIER>_<AREA>' from its membership level and locality. */
public final class OwnerSegment {

    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    /** PREMIUM at membershipLevel 3 or more (else STANDARD), METRO for a known region (else REGIONAL). */
    public static String of(int membershipLevel, String locality) {
        String tier = membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
