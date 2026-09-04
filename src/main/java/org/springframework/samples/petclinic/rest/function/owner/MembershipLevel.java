package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Membership level rule: a numeric level derived from the owner's {@link MembershipPoints}
 * and capped at 4. The factors that earn points, and their weights, live in the small
 * factor classes behind {@link MembershipPoints}; this class owns only the mapping from a
 * points total to a level. A newly created owner has zero tenure, so a new owner never
 * exceeds 3.
 */
public final class MembershipLevel {

    private static final int CAP = 4;

    private MembershipLevel() {
    }

    public static int of(Owner owner) {
        return Math.min(MembershipPoints.of(owner), CAP);
    }
}
