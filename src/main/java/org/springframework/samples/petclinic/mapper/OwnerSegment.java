package org.springframework.samples.petclinic.mapper;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's marketing segment, formatted {@code '<TIER>_<AREA>'}.
 *
 * <p>TIER is {@code PREMIUM} when the owner's {@link MembershipLevel} is 3 or more, otherwise
 * {@code STANDARD}. AREA is {@code METRO} when the owner's {@link Locality} is a known region
 * (NSW, VIC or QLD), otherwise {@code REGIONAL}. So the segment is one of {@code PREMIUM_METRO},
 * {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or {@code STANDARD_REGIONAL}.
 *
 * <p>Kept as a standalone class (not a method on {@link OwnerMapper}) so MapStruct does not
 * mistake it for an implicit mapping method, mirroring {@link Locality} and {@link Timezone}.
 */
public final class OwnerSegment {

    /** The known regions whose locality counts as METRO. */
    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    /** The {@code '<TIER>_<AREA>'} segment for {@code owner}. */
    public static String of(Owner owner) {
        String tier = MembershipLevel.of(owner) >= 3 ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(Locality.ofMemberId(owner.getMemberId()))
            ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
