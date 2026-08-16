package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Derives an owner's segment, formatted {@code <TIER>_<AREA>} — one of {@code PREMIUM_METRO},
 * {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or {@code STANDARD_REGIONAL}.
 *
 * <p>TIER is {@code PREMIUM} when {@code membershipLevel} is 3 or more, otherwise {@code STANDARD}.
 * AREA is {@code METRO} when the locality is a known region (NSW, VIC or QLD), otherwise
 * {@code REGIONAL}.
 */
public final class OwnerSegment {

    private OwnerSegment() {
    }

    /** The {@code <TIER>_<AREA>} segment for the given membership level and locality/region. */
    public static String of(Integer membershipLevel, String locality) {
        String tier = membershipLevel != null && membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = isKnownRegion(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    private static boolean isKnownRegion(String locality) {
        return "NSW".equals(locality) || "VIC".equals(locality) || "QLD".equals(locality);
    }
}
