package org.springframework.samples.petclinic.model;

/**
 * Derives an owner's marketing segment, formatted {@code <TIER>_<AREA>}: one of
 * {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or
 * {@code STANDARD_REGIONAL}.
 *
 * <p>TIER is {@code PREMIUM} when the owner's {@link MembershipLevel} is 3 or more,
 * otherwise {@code STANDARD}. AREA is {@code METRO} when the owner's {@link Locality} is a
 * known region (NSW, VIC or QLD), otherwise {@code REGIONAL}.
 */
public final class OwnerSegment {

    private OwnerSegment() {
    }

    /** The owner's marketing segment, formatted {@code <TIER>_<AREA>}. */
    public static String of(Owner owner) {
        String tier = MembershipLevel.of(owner) >= 3 ? "PREMIUM" : "STANDARD";
        String area = Locality.UNKNOWN.equals(Locality.of(owner)) ? "REGIONAL" : "METRO";
        return tier + "_" + area;
    }
}
