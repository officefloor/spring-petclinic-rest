package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Aggregate membership points for an owner: a starting allowance plus the points
 * contributed by each qualifying factor. Adding or reweighting a factor is a change to a
 * single small factor class ({@link EmailFactor}, {@link NamesakeFactor},
 * {@link TenureFactor}) or to this sum, not to the level-mapping in {@link MembershipLevel}.
 */
public final class MembershipPoints {

    private static final int BASE = 1;

    private MembershipPoints() {
    }

    public static int of(Owner owner) {
        return BASE + EmailFactor.points(owner) + NamesakeFactor.points(owner) + TenureFactor.points(owner);
    }
}
