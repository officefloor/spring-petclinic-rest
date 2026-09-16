package org.springframework.samples.petclinic.model;

/**
 * The single definition of an owner's <em>segment</em>, formatted {@code <TIER>_<AREA>}: one of
 * {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or
 * {@code STANDARD_REGIONAL}.
 *
 * <p>TIER is {@code PREMIUM} when the owner's membership level is 3 or more, otherwise
 * {@code STANDARD}. AREA is {@code METRO} when the owner's locality is a known region (NSW, VIC or
 * QLD; see {@link Locality#isKnownRegion}), otherwise {@code REGIONAL}.
 *
 * <p>Returned by the response mapper alongside the owner's other derived fields.
 */
public final class OwnerSegment {

    private OwnerSegment() {
    }

    /** The segment for an owner with the given membership level and locality. */
    public static String of(int membershipLevel, String locality) {
        String tier = membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = Locality.isKnownRegion(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
