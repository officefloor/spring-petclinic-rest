package org.springframework.samples.petclinic.util;

import java.util.Set;

/**
 * Derives an owner's segment string {@code '<TIER>_<AREA>'}. TIER is {@code PREMIUM}
 * when the membership level is 3 or more, otherwise {@code STANDARD}. AREA is
 * {@code METRO} when the locality is a known region (NSW, VIC or QLD), otherwise
 * {@code REGIONAL}.
 */
public final class Segments {

    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private Segments() {
    }

    /** The {@code '<TIER>_<AREA>'} segment for the given membership level and locality. */
    public static String of(int membershipLevel, String locality) {
        String tier = membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
