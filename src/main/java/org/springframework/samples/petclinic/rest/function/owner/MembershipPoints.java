package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Aggregate the membership points an owner earns from entity-derivable factors
 * ({@link EmailFactor}, {@link NamesakeFactor}, {@link TenureFactor}). The household
 * factor needs the repository to count members, so it is added at response time by
 * {@link Membership}; this sum feeds the level-mapping in {@link MembershipLevel}.
 */
public final class MembershipPoints {

    private static final int BASE = 0;

    private MembershipPoints() {
    }

    public static int of(Owner owner) {
        return BASE + EmailFactor.points(owner) + NamesakeFactor.points(owner) + TenureFactor.points(owner);
    }
}
