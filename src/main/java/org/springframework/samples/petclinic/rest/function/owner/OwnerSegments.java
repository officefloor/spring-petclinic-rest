package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the owner's {@code ownerSegment}, formatted {@code <TIER>_<AREA>}, the single classification
 * both the owner response and the create audit event hang off.
 *
 * <ul>
 * <li>TIER is {@code PREMIUM} when the owner's {@code membershipLevel} is {@value #PREMIUM_MIN_LEVEL}
 * or more, otherwise {@code STANDARD};</li>
 * <li>AREA is {@code METRO} when the owner's {@link MemberIds#plainRegionOf(Owner) plain region} is a
 * known region (NSW, VIC or QLD), otherwise {@code REGIONAL}.</li>
 * </ul>
 *
 * <p>The AREA is derived from the plain, user-facing region — the version-2 {@code "V2"} identifier
 * tag baked into the {@code memberId}, {@code householdId} and {@code identityKey} never reaches it.
 */
public final class OwnerSegments {

    /** The lowest {@code membershipLevel} that grades an owner's segment TIER as {@code PREMIUM}. */
    private static final int PREMIUM_MIN_LEVEL = 3;

    /** The known regions whose owners are graded {@code METRO} (mirrors the region-to-timezone table). */
    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegments() {
    }

    /** The {@code <TIER>_<AREA>} segment label for the owner at the given (capped) membership level. */
    public static String label(Owner owner, int membershipLevel) {
        String tier = membershipLevel >= PREMIUM_MIN_LEVEL ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(MemberIds.plainRegionOf(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
