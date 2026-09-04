package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Membership points contributed by the owner having an email address. One factor of the
 * overall {@link MembershipPoints} total; owns the "has an email" test and its weight.
 */
public final class EmailFactor {

    private static final int POINTS = 1;

    private EmailFactor() {
    }

    public static int points(Owner owner) {
        return present(owner) ? POINTS : 0;
    }

    private static boolean present(Owner owner) {
        return owner.getEmail() != null && !owner.getEmail().isBlank();
    }
}
