package org.springframework.samples.petclinic.rest.function.common;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the owner's {@code ownerSegment}, formatted {@code '<TIER>_<AREA>'} — one of
 * {@code 'PREMIUM_METRO'}, {@code 'PREMIUM_REGIONAL'}, {@code 'STANDARD_METRO'} or
 * {@code 'STANDARD_REGIONAL'}.
 *
 * <p>{@code TIER} is {@code 'PREMIUM'} when the owner's numeric {@code membershipLevel} is 3 or more,
 * otherwise {@code 'STANDARD'}. {@code AREA} is {@code 'METRO'} when the owner's locality is a known
 * region (NSW, VIC or QLD, see {@link Localities}), otherwise {@code 'REGIONAL'}.
 */
public final class OwnerSegments {

    /** The regions considered METRO; every other (or "UNKNOWN") locality is REGIONAL. */
    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    /** The numeric membership level at or above which an owner is PREMIUM. */
    private static final int PREMIUM_LEVEL = 3;

    private OwnerSegments() {
    }

    /** The owner's {@code '<TIER>_<AREA>'} segment. */
    public static String of(Owner owner) {
        int level = owner.getMembershipLevel() != null ? owner.getMembershipLevel()
                : Membership.levelOf(owner);
        String tier = level >= PREMIUM_LEVEL ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(CustomerCodes.localityOf(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
