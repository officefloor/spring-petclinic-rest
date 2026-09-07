package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Marketing segment for a pet owner, formatted {@code <TIER>_<AREA>}, one of {@code PREMIUM_METRO},
 * {@code PREMIUM_REGIONAL}, {@code STANDARD_METRO} or {@code STANDARD_REGIONAL}. TIER is
 * {@code PREMIUM} when the owner's {@link MembershipLevel} is 3 or more, otherwise {@code STANDARD}.
 * AREA is {@code METRO} when the owner's {@link Locality} is a known region ({@code NSW}, {@code VIC}
 * or {@code QLD}), otherwise {@code REGIONAL} (including the {@code UNKNOWN} locality). Derived purely
 * from the owner's own state (its membership level and locality), so it carries no stored data and is
 * seed-independent. Used by the owner mapper to expose {@code ownerSegment} on responses.
 */
public final class OwnerSegment {

    /** The canonical regions treated as metropolitan areas. */
    private static final Set<String> METRO_REGIONS = Set.of("NSW", "VIC", "QLD");

    private OwnerSegment() {
    }

    /**
     * The segment for the given membership level and locality: {@code PREMIUM} when the level is 3
     * or more else {@code STANDARD}, joined by {@code _} to {@code METRO} when the locality is a
     * known region ({@code NSW}, {@code VIC}, {@code QLD}) else {@code REGIONAL}.
     */
    public static String of(int membershipLevel, String locality) {
        String tier = membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = METRO_REGIONS.contains(locality) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /**
     * The segment for the given owner, computed from its effective {@link MembershipLevel} and its
     * {@link Locality}.
     */
    public static String of(Owner owner) {
        return of(MembershipLevel.of(owner), Locality.of(owner));
    }
}
