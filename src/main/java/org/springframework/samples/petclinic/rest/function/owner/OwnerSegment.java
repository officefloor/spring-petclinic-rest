package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's marketing segment, {@code ownerSegment}, formatted {@code <TIER>_<AREA>}.
 *
 * <p>TIER is {@code PREMIUM} when the owner's {@link Owner#getMembershipLevel() membershipLevel}
 * is 3 or more, otherwise {@code STANDARD}. AREA is {@code METRO} when the owner's
 * {@link OwnerLocality#forOwner(Owner) locality} is a known region (NSW, VIC or QLD), otherwise
 * {@code REGIONAL}. The four possible values are {@code PREMIUM_METRO}, {@code PREMIUM_REGIONAL},
 * {@code STANDARD_METRO} and {@code STANDARD_REGIONAL}.
 */
public final class OwnerSegment {

    /** The known regions that resolve to a METRO area. */
    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    public static String forOwner(Owner owner) {
        Integer level = owner.getMembershipLevel();
        String tier = (level != null && level >= 3) ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(OwnerLocality.forOwner(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
