package org.springframework.samples.petclinic.rest.function.common;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Derives an owner's {@link OwnerDto.OwnerSegmentEnum segment}, formatted {@code "<TIER>_<AREA>"}.
 *
 * <p>TIER is {@code PREMIUM} when the owner's {@link MembershipLevels#of(Owner) membership level} is
 * 3 or more, otherwise {@code STANDARD}. AREA is {@code METRO} when the owner's
 * {@link Localities#region(String, String) locality} is a known region (NSW, VIC or QLD), otherwise
 * {@code REGIONAL}. The possible values are {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL},
 * {@code STANDARD_METRO} and {@code STANDARD_REGIONAL}.
 */
public final class OwnerSegments {

    /** Membership level at or above which the owner is in the PREMIUM tier. */
    private static final int PREMIUM_LEVEL = 3;

    private OwnerSegments() {
    }

    /** Computes the owner's segment. */
    public static OwnerDto.OwnerSegmentEnum of(Owner owner) {
        String tier = MembershipLevels.of(owner) >= PREMIUM_LEVEL ? "PREMIUM" : "STANDARD";
        String region = Localities.region(owner.getPostcode(), owner.getCity());
        String area = Localities.UNKNOWN.equals(region) ? "REGIONAL" : "METRO";
        return OwnerDto.OwnerSegmentEnum.valueOf(tier + "_" + area);
    }
}
