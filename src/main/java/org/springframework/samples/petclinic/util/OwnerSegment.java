package org.springframework.samples.petclinic.util;

import java.util.Set;

/**
 * Classifies an owner into a market segment formatted {@code <TIER>_<AREA>}, one of
 * {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or
 * {@code STANDARD_REGIONAL}.
 *
 * <p>The tier is {@code PREMIUM} when the membership level is 3 or more, otherwise
 * {@code STANDARD}. The area is {@code METRO} when the locality is a known region
 * (NSW, VIC or QLD), otherwise {@code REGIONAL}. This class owns only that
 * classification, keeping it separate from how the membership level and locality are
 * derived (see {@link CityLocality}).
 */
public final class OwnerSegment {

    /** Membership level at or above which an owner is {@code PREMIUM} rather than {@code STANDARD}. */
    private static final int PREMIUM_MIN_LEVEL = 3;

    /** Localities treated as {@code METRO}; any other locality is {@code REGIONAL}. */
    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    /**
     * @param membershipLevel the owner's membership level
     * @param locality        the owner's locality, as resolved by {@link CityLocality}
     * @return the {@code <TIER>_<AREA>} segment
     */
    public static String of(int membershipLevel, String locality) {
        String tier = membershipLevel >= PREMIUM_MIN_LEVEL ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
