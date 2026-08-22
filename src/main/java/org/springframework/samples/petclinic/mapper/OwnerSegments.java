package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.MembershipLevels;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Derives an owner's marketing segment, formatted {@code <TIER>_<AREA>}. TIER is
 * {@code PREMIUM} when the membershipLevel is 3 or more, otherwise {@code STANDARD};
 * AREA is {@code METRO} when the locality is a known region (NSW, VIC or QLD),
 * otherwise {@code REGIONAL}. Kept as a plain static helper (not a mapper method) so
 * MapStruct does not treat it as an implicit mapping method.
 */
public final class OwnerSegments {

    private OwnerSegments() {
    }

    /** The owner's segment, combining membership tier with locality area. */
    public static OwnerDto.OwnerSegmentEnum of(Owner owner) {
        int membershipLevel = owner.getMembershipLevel() != null ? owner.getMembershipLevel()
                : MembershipLevels.of(owner);
        boolean premium = membershipLevel >= 3;
        boolean metro = !"UNKNOWN".equals(Localities.region(owner));
        String tier = premium ? "PREMIUM" : "STANDARD";
        String area = metro ? "METRO" : "REGIONAL";
        return OwnerDto.OwnerSegmentEnum.valueOf(tier + "_" + area);
    }
}
