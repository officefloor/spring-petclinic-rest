package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Marketing segment for an owner, formatted {@code <TIER>_<AREA>}. TIER is {@code PREMIUM} at
 * membership level 3 or more, otherwise {@code STANDARD}; AREA is {@code METRO} when the owner's
 * {@link OwnerLocality#region locality} is a known region (NSW, VIC or QLD), otherwise
 * {@code REGIONAL}.
 */
public final class OwnerSegment {

    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    /** The owner's segment, e.g. {@code "STANDARD_METRO"}. */
    public static String of(Owner owner) {
        return tier(owner) + "_" + area(owner);
    }

    private static String tier(Owner owner) {
        Integer level = owner.getMembershipLevel();
        int effective = level != null ? level : OwnerMembershipLevel.of(owner);
        return effective >= 3 ? "PREMIUM" : "STANDARD";
    }

    private static String area(Owner owner) {
        return METRO_REGIONS.contains(OwnerLocality.region(owner)) ? "METRO" : "REGIONAL";
    }
}
