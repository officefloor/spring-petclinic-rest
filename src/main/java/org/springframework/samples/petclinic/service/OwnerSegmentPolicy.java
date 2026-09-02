package org.springframework.samples.petclinic.service;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: an owner's {@code ownerSegment} is {@code <TIER>_<AREA>}. TIER is {@code PREMIUM}
 * when {@code membershipLevel} is 3 or more, otherwise {@code STANDARD}. AREA is {@code METRO} when
 * the owner's locality is a known region (NSW, VIC or QLD), otherwise {@code REGIONAL}. Kept as a
 * small, self-contained unit so the rule can be applied from the read flow without adding
 * complexity to the mapper, controller, or service.
 */
public final class OwnerSegmentPolicy {

    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegmentPolicy() {
    }

    /**
     * Derive the {@code ownerSegment} for the given owner.
     *
     * @param owner the owner whose segment to derive
     * @return the segment, one of the four {@code <TIER>_<AREA>} combinations
     */
    public static String ownerSegment(Owner owner) {
        String tier = OwnerMembershipCeilingPolicy.cappedMembershipLevel(owner) >= 3 ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(OwnerLocalityPolicy.locality(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
