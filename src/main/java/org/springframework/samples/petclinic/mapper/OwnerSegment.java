package org.springframework.samples.petclinic.mapper;

import java.util.Set;

import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto.OwnerSegmentEnum;

/**
 * Derives an owner's marketing segment ('ownerSegment'), formatted {@code <TIER>_<AREA>}: one of
 * {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or
 * {@code STANDARD_REGIONAL}.
 *
 * <p>TIER is {@code PREMIUM} when the owner's {@link MembershipLevel#of(Owner) membershipLevel} is 3
 * or more, otherwise {@code STANDARD}. AREA is {@code METRO} when the owner's
 * {@link Locality#of(String) locality} is a known region (NSW, VIC or QLD), otherwise
 * {@code REGIONAL}.
 *
 * <p>Kept as a standalone helper (referenced from {@link OwnerMapper}'s {@code ownerSegment}
 * expression) rather than a mapper {@code default} method to avoid MapStruct picking it up as an
 * automatic conversion.
 */
final class OwnerSegment {

    /** Regions that count as METRO. Anything else (including {@link Locality#UNKNOWN}) is REGIONAL. */
    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    /** Minimum membership level for the PREMIUM tier. */
    private static final int PREMIUM_MIN_LEVEL = 3;

    private OwnerSegment() {
    }

    /** The {@code <TIER>_<AREA>} segment for the given owner. */
    static OwnerSegmentEnum of(Owner owner) {
        String tier = MembershipLevel.of(owner) >= PREMIUM_MIN_LEVEL ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(Locality.of(owner.getCustomerCode())) ? "METRO" : "REGIONAL";
        return OwnerSegmentEnum.fromValue(tier + "_" + area);
    }
}
