package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's marketing segment, formatted {@code '<TIER>_<AREA>'} — one of
 * {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or
 * {@code STANDARD_REGIONAL}.
 *
 * <p>TIER is {@code PREMIUM} when the owner's {@link #level(Owner) membership level} is 3 or
 * more, otherwise {@code STANDARD}. AREA is {@code METRO} when the owner's locality (the region
 * component of the {@link CustomerCode customer code}) is a known region — NSW, VIC or QLD —
 * otherwise {@code REGIONAL}.
 */
public final class OwnerSegment {

    /** Membership level at or above which an owner is in the PREMIUM tier. */
    private static final int PREMIUM_THRESHOLD = 3;

    private OwnerSegment() {
    }

    /** The {@code '<TIER>_<AREA>'} segment for {@code owner}. */
    public static String of(Owner owner) {
        String tier = level(owner) >= PREMIUM_THRESHOLD ? "PREMIUM" : "STANDARD";
        String region = CustomerCode.region(owner.getCustomerCode());
        String area = Locality.timezone(region) != null ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /** The owner's effective membership level: the stored value when present, else the derived one. */
    private static int level(Owner owner) {
        return owner.getMembershipLevel() != null ? owner.getMembershipLevel() : MembershipLevel.of(owner);
    }
}
