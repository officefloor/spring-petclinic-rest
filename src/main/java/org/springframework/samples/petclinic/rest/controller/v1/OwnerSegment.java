package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's segment formatted {@code <TIER>_<AREA>}: TIER is {@code PREMIUM} when the
 * owner's {@link MembershipCap} level is 3 or more else {@code STANDARD}, and AREA is {@code METRO}
 * when its {@link CustomerCodes} region is a known region (NSW, VIC or QLD) else {@code REGIONAL}.
 */
public final class OwnerSegment {

    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    /** The {@code <TIER>_<AREA>} segment for {@code owner}. */
    public static String of(Owner owner) {
        String tier = MembershipCap.level(owner) >= 3 ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(CustomerCodes.region(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
