package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Derives an owner's {@code ownerSegment}, formatted {@code <TIER>_<AREA>} — one of
 * {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or
 * {@code STANDARD_REGIONAL}.
 *
 * <ul>
 *   <li>TIER is {@code PREMIUM} when the owner's {@link MembershipLevel} is {@code 3} or more,
 *       otherwise {@code STANDARD}.</li>
 *   <li>AREA is {@code METRO} when the owner's locality (see {@link CityRegion}) is a known region
 *       (NSW, VIC or QLD), otherwise {@code REGIONAL}.</li>
 * </ul>
 */
public final class OwnerSegment {

    private static final int PREMIUM_MIN_LEVEL = 3;

    private OwnerSegment() {
    }

    /** The owner's segment, formatted {@code <TIER>_<AREA>}. */
    public static OwnerDto.OwnerSegmentEnum of(Owner owner, OwnerRepository ownerRepository) {
        String tier = MembershipLevel.of(owner, ownerRepository) >= PREMIUM_MIN_LEVEL ? "PREMIUM" : "STANDARD";
        String locality = CityRegion.localityOfCustomerCode(owner.getCustomerCode(), owner.getCity(),
            owner.getPostcode());
        String area = CityRegion.UNKNOWN.equals(locality) ? "REGIONAL" : "METRO";
        return OwnerDto.OwnerSegmentEnum.valueOf(tier + "_" + area);
    }
}
