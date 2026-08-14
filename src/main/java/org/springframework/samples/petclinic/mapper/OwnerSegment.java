package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Derives an owner's {@link OwnerDto.OwnerSegmentEnum marketing segment} {@code '<TIER>_<AREA>'}.
 *
 * <p>TIER is {@code PREMIUM} when the owner's {@link MembershipLevel#effective(Owner) effective}
 * membership level is 3 or more, otherwise {@code STANDARD}. AREA is {@code METRO} when the owner's
 * {@link CustomerCode#locality(Owner) locality} is a known region (NSW, VIC or QLD), otherwise
 * {@code REGIONAL}. The four combinations are {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL},
 * {@code STANDARD_METRO} and {@code STANDARD_REGIONAL}.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so MapStruct does not
 * mistake it for a mapping method and apply it to unrelated fields.
 */
public final class OwnerSegment {

    private OwnerSegment() {
    }

    /** The {@code '<TIER>_<AREA>'} segment for {@code owner}. */
    public static OwnerDto.OwnerSegmentEnum of(Owner owner) {
        boolean premium = MembershipLevel.effective(owner) >= 3;
        boolean metro = CityRegion.timezone(CustomerCode.locality(owner)) != null;
        if (premium) {
            return metro ? OwnerDto.OwnerSegmentEnum.PREMIUM_METRO : OwnerDto.OwnerSegmentEnum.PREMIUM_REGIONAL;
        }
        return metro ? OwnerDto.OwnerSegmentEnum.STANDARD_METRO : OwnerDto.OwnerSegmentEnum.STANDARD_REGIONAL;
    }
}
