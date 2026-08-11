package org.springframework.samples.petclinic.mapper;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's marketing segment, formatted {@code '<TIER>_<AREA>'} — one of
 * {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or
 * {@code STANDARD_REGIONAL}. TIER is {@code PREMIUM} when the owner's {@code membershipLevel}
 * (see {@link Membership}) is 3 or more, otherwise {@code STANDARD}. AREA is {@code METRO} when the
 * owner's {@link Locality} is a known region (NSW, VIC or QLD), otherwise {@code REGIONAL}. Kept out
 * of {@link OwnerMapper} so MapStruct does not mistake the helper for an implicit mapping method.
 */
public final class OwnerSegment {

    /** The membership level (inclusive) at and above which an owner is {@code PREMIUM}. */
    private static final int PREMIUM_LEVEL = 3;

    /** Localities counted as {@code METRO}; anything else is {@code REGIONAL}. */
    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    /** The {@code '<TIER>_<AREA>'} segment for {@code owner}. */
    public static String of(Owner owner) {
        int level = owner.getMembershipLevel() != null ? owner.getMembershipLevel()
            : Membership.levelOf(owner);
        String tier = level >= PREMIUM_LEVEL ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(Locality.of(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
