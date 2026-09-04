package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import org.springframework.samples.petclinic.model.Locality;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Owner segment rule: {@code '<TIER>_<AREA>'}. TIER is {@code PREMIUM} at membershipLevel 3
 * or more, otherwise {@code STANDARD}. AREA is {@code METRO} when the locality is a known
 * region (NSW, VIC or QLD), otherwise {@code REGIONAL}.
 */
public final class OwnerSegment {

    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    public static String of(Owner owner) {
        String tier = MembershipLevel.of(MembershipPoints.of(owner)) >= 3 ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(Locality.of(owner.getCity(), owner.getPostcode())) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
