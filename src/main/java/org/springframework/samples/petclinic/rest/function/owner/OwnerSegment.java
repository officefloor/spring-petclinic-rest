package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

/**
 * Derives an owner's {@code ownerSegment}, formatted {@code <TIER>_<AREA>} and always one of
 * {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or
 * {@code STANDARD_REGIONAL}. TIER is {@code PREMIUM} when {@code membershipLevel} is 3 or more,
 * otherwise {@code STANDARD}. AREA is {@code METRO} when the locality is a known region
 * (NSW, VIC or QLD, see {@link Locality}), otherwise {@code REGIONAL}. Pinned and stable across
 * requests, so it needs no persisted column.
 */
public final class OwnerSegment {

    /** Membership level at or above which the owner is in the PREMIUM tier. */
    private static final int PREMIUM_THRESHOLD = 3;

    /** Regions treated as METRO; anything else (including "UNKNOWN") is REGIONAL. */
    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    /** The segment for the given {@code locality} region and {@code membershipLevel}. */
    public static String of(String locality, Integer membershipLevel) {
        String tier = membershipLevel != null && membershipLevel >= PREMIUM_THRESHOLD ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
