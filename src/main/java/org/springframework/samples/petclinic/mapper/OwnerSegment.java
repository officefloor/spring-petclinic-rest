package org.springframework.samples.petclinic.mapper;

import java.util.Set;

/**
 * Derives an owner's marketing segment, formatted {@code '<TIER>_<AREA>'}.
 *
 * <p>The tier is {@code "PREMIUM"} when the {@code membershipLevel} is 3 or more, otherwise
 * {@code "STANDARD"} (including when the level is absent). The area is {@code "METRO"} when the
 * {@code locality} is a known region (NSW, VIC or QLD), otherwise {@code "REGIONAL"}. The two are
 * joined with an underscore, yielding one of {@code "PREMIUM_METRO"}, {@code "PREMIUM_REGIONAL"},
 * {@code "STANDARD_METRO"} or {@code "STANDARD_REGIONAL"}.
 *
 * <p>Kept as a standalone helper rather than a method on {@link OwnerMapper}: a single-argument
 * method declared on a MapStruct mapper would be picked up as an implicit conversion and applied to
 * every matching property mapping.
 */
public final class OwnerSegment {

    /** Localities that count as METRO. */
    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    /**
     * Returns the {@code '<TIER>_<AREA>'} segment for {@code membershipLevel} and {@code locality}.
     */
    public static String of(Integer membershipLevel, String locality) {
        String tier = membershipLevel != null && membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = locality != null && METRO_REGIONS.contains(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
