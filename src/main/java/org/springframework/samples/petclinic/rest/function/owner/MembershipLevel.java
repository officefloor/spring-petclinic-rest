package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the owner's numeric membership level, from 1 to 3. Starts at 1; adds 1 when an
 * email is present; adds 1 when the namesake count is 0; capped at 3. Level 4 is reserved
 * for tenure.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    public static int of(Owner owner, int namesakeCount) {
        int level = 1;
        if (owner.getEmail() != null) {
            level++;
        }
        if (namesakeCount == 0) {
            level++;
        }
        return Math.min(level, 3);
    }
}
