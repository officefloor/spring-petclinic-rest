package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The single definition of an owner's segment, formatted {@code '<TIER>_<AREA>'} and one of
 * {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or
 * {@code STANDARD_REGIONAL}. TIER is {@code PREMIUM} at membershipLevel 3 or more, otherwise
 * {@code STANDARD}; AREA is {@code METRO} for a known region (see {@link Locality#isKnown}),
 * otherwise {@code REGIONAL}.
 *
 * <p>A pure function of the membership level and region, so it is composed on read rather than
 * stored.
 */
public final class OwnerSegment {

    private OwnerSegment() {
    }

    /** The segment for an owner with the given {@code membershipLevel} and canonical
     *  {@code region} (see {@link Locality}). A missing membership level is treated as below
     *  the premium threshold. */
    public static String of(Integer membershipLevel, String region) {
        String tier = membershipLevel != null && membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = Locality.isKnown(region) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
