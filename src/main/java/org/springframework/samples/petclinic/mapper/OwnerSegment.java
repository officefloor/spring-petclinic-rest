package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the {@code ownerSegment} field produced by {@link OwnerMapper#toOwnerDto}: a coarse
 * marketing segment formatted {@code <TIER>_<AREA>}, one of {@code PREMIUM_METRO},
 * {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or {@code STANDARD_REGIONAL}.
 *
 * <p>{@code TIER} is {@code PREMIUM} when the owner's {@link MembershipLevel#of(Owner) membership
 * level} is {@link #PREMIUM_FROM} (3) or more, otherwise {@code STANDARD}. {@code AREA} is
 * {@code METRO} when the owner's plain region (see {@link Regions#regionForPostcode(String)}) is a
 * {@link Regions#isKnownRegion(String) known region} (NSW, VIC or QLD), otherwise {@code REGIONAL}.
 *
 * <p>The region is recomputed from the postcode, not read back out of the version-2 {@code memberId}:
 * the member id now carries the {@code V2} identity-version tag inside its region component, which must
 * never leak into the owner segment, so the segment derives the plain region directly.
 */
public final class OwnerSegment {

    /** Minimum membership level (inclusive) for the {@code PREMIUM} tier; below this is STANDARD. */
    private static final int PREMIUM_FROM = 3;

    private OwnerSegment() {
    }

    /**
     * Derive the segment for an owner.
     *
     * @param owner the owner; its membership level and locality drive the result
     * @return the segment, formatted {@code <TIER>_<AREA>}
     */
    public static String of(Owner owner) {
        String tier = MembershipLevel.of(owner) >= PREMIUM_FROM ? "PREMIUM" : "STANDARD";
        String locality = Regions.regionForPostcode(owner.getPostcode());
        String area = Regions.isKnownRegion(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
