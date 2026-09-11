package org.springframework.samples.petclinic.mapper;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.owner.CustomerCodes;

/**
 * Derives an owner's {@code ownerSegment}, formatted {@code <TIER>_<AREA>}.
 *
 * <p>{@code TIER} is {@code PREMIUM} when the owner's {@link MembershipLevels#forOwner(Owner)
 * membershipLevel} is 3 or more, otherwise {@code STANDARD}. {@code AREA} is {@code METRO}
 * when the owner's {@link CustomerCodes#localityOf(Owner) locality} is a known region
 * (NSW, VIC or QLD), otherwise {@code REGIONAL}.
 */
public final class OwnerSegments {

    /** Membership level at or above which an owner is in the PREMIUM tier. */
    private static final int PREMIUM_MIN_LEVEL = 3;

    /** Localities that count as METRO; anything else is REGIONAL. */
    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegments() {
    }

    /** The {@code <TIER>_<AREA>} segment for the given owner. */
    public static String forOwner(Owner owner) {
        String tier = MembershipLevels.forOwner(owner) >= PREMIUM_MIN_LEVEL ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(CustomerCodes.localityOf(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
