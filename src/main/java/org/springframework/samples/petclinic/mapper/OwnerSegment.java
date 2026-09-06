package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

import java.util.Set;

/**
 * Derives an owner's segment, formatted {@code <TIER>_<AREA>}, from the owner's
 * membership level and locality (region). Kept as a plain static helper - rather
 * than a method on {@link OwnerMapper} - so MapStruct does not mistake it for an
 * implicit property mapping.
 *
 * <p>TIER is {@code PREMIUM} when the owner's {@link MembershipLevel} is
 * {@value #PREMIUM_MEMBERSHIP_LEVEL} or more, otherwise {@code STANDARD}. AREA is
 * {@code METRO} when the owner's {@link OwnerLocality locality} is one of the
 * known regions ({@code NSW}, {@code VIC} or {@code QLD}), otherwise
 * {@code REGIONAL}. The result is therefore one of {@code PREMIUM_METRO},
 * {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or {@code STANDARD_REGIONAL}.
 */
public final class OwnerSegment {

    /** Membership level at or above which the owner is in the PREMIUM tier. */
    private static final int PREMIUM_MEMBERSHIP_LEVEL = 3;

    /** Known regions that place the owner in the METRO area. */
    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    /**
     * Returns the owner's segment, formatted {@code <TIER>_<AREA>}.
     *
     * @param owner the owner whose segment to derive
     * @return one of {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL},
     *         {@code STANDARD_METRO} or {@code STANDARD_REGIONAL}
     */
    public static String forOwner(Owner owner) {
        String tier = MembershipLevel.forOwner(owner) >= PREMIUM_MEMBERSHIP_LEVEL ? "PREMIUM" : "STANDARD";
        String region = OwnerLocality.forMemberId(owner.getMemberId());
        String area = METRO_REGIONS.contains(region) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

}
