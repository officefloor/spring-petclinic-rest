package org.springframework.samples.petclinic.mapper;

import java.util.Set;

import org.springframework.samples.petclinic.model.MembershipLevels;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's segment formatted '<TIER>_<AREA>'. TIER is 'PREMIUM' when the
 * (capped) membershipLevel is 3 or more, otherwise 'STANDARD'. AREA is 'METRO' when
 * the locality is a known region (NSW, VIC or QLD), otherwise 'REGIONAL'.
 */
final class OwnerSegments {

    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegments() {
    }

    /** The '<TIER>_<AREA>' segment for {@code owner}. */
    static String of(Owner owner) {
        int level = MembershipLevels.levelOf(owner);
        Integer cap = owner.getMembershipLevelCap();
        if (cap != null && cap < level) {
            level = cap;
        }
        String tier = level >= 3 ? "PREMIUM" : "STANDARD";
        String region = owner.getCustomerCode() == null ? null : owner.getCustomerCode().split("-")[0];
        String area = METRO_REGIONS.contains(region) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
