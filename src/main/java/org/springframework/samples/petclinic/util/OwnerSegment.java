package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's marketing segment '&lt;TIER&gt;_&lt;AREA&gt;'. TIER is 'PREMIUM' when the
 * owner's membership level is 3 or more, otherwise 'STANDARD'. AREA is 'METRO' when the owner's
 * {@link OwnerRegion region} is a {@link CityRegion#isKnown known region} (NSW, VIC or QLD),
 * otherwise 'REGIONAL'.
 *
 * <p>The segment is derived from the owner's plain {@link OwnerRegion region}, never the version
 * tagged region embedded in its identifiers, so the {@link OwnerIdentityVersion#TAG version tag}
 * never leaks into the segment.
 */
public final class OwnerSegment {

    private OwnerSegment() {
    }

    /**
     * The segment for an owner with the given {@code membershipLevel} (null counts as below the
     * premium threshold) and canonical {@code region}.
     */
    public static String of(Integer membershipLevel, String region) {
        String tier = membershipLevel != null && membershipLevel >= 3 ? "PREMIUM" : "STANDARD";
        String area = CityRegion.isKnown(region) ? "METRO" : "REGIONAL";
        return tier + "_" + area;
    }

    /** The segment for the given {@code owner}, keyed off its membership level and plain region. */
    public static String of(Owner owner) {
        return of(owner.getMembershipLevel(), OwnerRegion.of(owner));
    }
}
