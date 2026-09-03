package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's segment as {@code <TIER>_<AREA>}: TIER is {@code PREMIUM} at
 * membershipLevel 3 or more else {@code STANDARD}; AREA is {@code METRO} when the owner's
 * locality is a known region (NSW, VIC or QLD) else {@code REGIONAL}.
 */
public final class OwnerSegment {

    private static final Set<String> METRO = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    public static String of(int membershipLevel, Owner owner) {
        String tier = membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = METRO.contains(Locality.locality(owner)) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }
}
