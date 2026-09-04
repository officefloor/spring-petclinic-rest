package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Membership points contributed by the owner having no namesakes. One factor of the
 * overall {@link MembershipPoints} total; owns the "no namesakes" test and its weight.
 */
public final class NamesakeFactor {

    private static final int POINTS = 1;

    private NamesakeFactor() {
    }

    public static int points(Owner owner) {
        return none(owner) ? POINTS : 0;
    }

    private static boolean none(Owner owner) {
        return owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
    }
}
