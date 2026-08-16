package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's segment, formatted {@code '<TIER>_<AREA>'}, from the owner's
 * membership level and locality (region).
 *
 * <p>TIER is {@code PREMIUM} when the membership level is 3 or more, otherwise
 * {@code STANDARD}. AREA is {@code METRO} when the locality is a known region
 * (NSW, VIC or QLD), otherwise {@code REGIONAL}. The four possible values are
 * {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} and
 * {@code STANDARD_REGIONAL}.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so
 * MapStruct does not mistake it for a generic {@code String -> String} mapping
 * method and apply it to unrelated fields; the mapper references it only through an
 * explicit expression.
 */
public final class OwnerSegmentDeriver {

    private OwnerSegmentDeriver() {
    }

    /**
     * Returns the owner's segment formatted {@code '<TIER>_<AREA>'}: TIER is
     * {@code PREMIUM} when {@code membershipLevel} is 3 or more, else {@code STANDARD};
     * AREA is {@code METRO} when {@code locality} is a known region (NSW, VIC or QLD),
     * else {@code REGIONAL}.
     */
    public static String ownerSegment(int membershipLevel, String locality) {
        String tier = membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = isKnownRegion(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    private static boolean isKnownRegion(String locality) {
        if (locality == null) {
            return false;
        }
        return switch (locality) {
            case "NSW", "VIC", "QLD" -> true;
            default -> false;
        };
    }
}
