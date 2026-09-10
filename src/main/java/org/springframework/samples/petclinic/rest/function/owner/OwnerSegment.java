package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.Localities;
import org.springframework.samples.petclinic.rest.function.common.MembershipLevels;

/**
 * Derives an owner's segment as {@code '<TIER>_<AREA>'}, one of {@code PREMIUM_METRO},
 * {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or {@code STANDARD_REGIONAL}.
 *
 * <p>TIER is {@code PREMIUM} when the owner's membershipLevel is 3 or more, otherwise
 * {@code STANDARD}. AREA is {@code METRO} when the owner's locality is a known region
 * (NSW, VIC or QLD), otherwise {@code REGIONAL}.
 */
public final class OwnerSegment {

    /** The tier threshold: a membershipLevel of this or more is {@code PREMIUM}. */
    static final int PREMIUM_LEVEL = 3;

    /** The known regions that count as {@code METRO}. */
    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    public static String of(Owner owner) {
        int level = owner.getMembershipLevel() != null ? owner.getMembershipLevel()
                : MembershipLevels.of(owner);
        String tier = level >= PREMIUM_LEVEL ? "PREMIUM" : "STANDARD";
        String locality = Localities.localityOf(owner.getCustomerCode(), owner.getCity(),
                owner.getPostcode());
        String area = METRO_REGIONS.contains(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
