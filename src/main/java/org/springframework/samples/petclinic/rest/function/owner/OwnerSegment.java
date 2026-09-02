package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Derives an owner's segment as {@code <TIER>_<AREA>}. TIER is {@code PREMIUM} when the
 * membershipLevel is 3 or more, otherwise {@code STANDARD}. AREA is {@code METRO} when the locality
 * is a known region (NSW, VIC or QLD), otherwise {@code REGIONAL}.
 */
public final class OwnerSegment {

    private OwnerSegment() {
    }

    public static String of(int membershipLevel, String locality) {
        String tier = membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = switch (locality) {
            case "NSW", "VIC", "QLD" -> "METRO";
            default -> "REGIONAL";
        };
        return tier + "_" + area;
    }
}
