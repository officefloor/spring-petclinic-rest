package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the owner's segment, formatted {@code <TIER>_<AREA>}. TIER is {@code PREMIUM}
 * when the membership level is 3 or more, otherwise {@code STANDARD}. AREA is {@code METRO}
 * when the {@link Locality} is a known region (NSW, VIC or QLD), otherwise {@code REGIONAL}.
 */
public final class OwnerSegment {

    private static final Set<String> KNOWN_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    public static String of(int membershipLevel, Owner owner) {
        String tier = membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = KNOWN_REGIONS.contains(Locality.of(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
