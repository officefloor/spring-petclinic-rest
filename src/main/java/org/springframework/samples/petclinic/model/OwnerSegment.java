package org.springframework.samples.petclinic.model;

/**
 * Classifies an owner into a market segment formatted {@code <TIER>_<AREA>}, one of
 * {@code "PREMIUM_METRO"}, {@code "PREMIUM_REGIONAL"}, {@code "STANDARD_METRO"} or
 * {@code "STANDARD_REGIONAL"}. TIER is {@code "PREMIUM"} when the membership level is 3 or more,
 * otherwise {@code "STANDARD"}. AREA is {@code "METRO"} when the locality is a known region (see
 * {@link CityRegion}), otherwise {@code "REGIONAL"}. Used to derive an owner's segment from its
 * membership level and locality.
 */
public final class OwnerSegment {

    private OwnerSegment() {
    }

    /**
     * The segment of an owner with membership {@code level} and {@code locality}. A {@code null}
     * level is treated as below the PREMIUM threshold; a {@code null} or {@link CityRegion#UNKNOWN}
     * locality as not a known region.
     */
    public static String classify(Integer level, String locality) {
        String tier = (level != null && level >= 3) ? "PREMIUM" : "STANDARD";
        boolean knownRegion = locality != null && !CityRegion.UNKNOWN.equals(locality);
        String area = knownRegion ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
