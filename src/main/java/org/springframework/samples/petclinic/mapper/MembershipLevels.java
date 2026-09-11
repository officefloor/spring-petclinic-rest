package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's numeric {@code membershipLevel} (1-3) from its create-time
 * attributes: start at 1, add 1 when an email is present, add 1 when
 * {@code namesakeCount} is 0, capped at 3. Level 4 is reserved for tenure.
 */
public final class MembershipLevels {

    private MembershipLevels() {
    }

    /** The membership level (1-3) for the given owner. */
    public static int forOwner(Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isEmpty()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        return Math.min(level, 3);
    }
}
