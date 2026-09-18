package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs in {@code POST /api/owners} after {@link AssignOwnerMembershipPoints} has recorded the
 * owner's membership points and before it is saved, within the create transaction. Records on the
 * built owner its numeric membership level, derived solely from those points: level 1 for 0-1
 * points, 2 for 2-3, 3 for 4-5, and 4 for 6 or more.
 */
public class AssignOwnerMembershipLevel {

    public void service(@Val Owner owner) {
        owner.setMembershipLevel(levelFor(owner.getMembershipPoints()));
    }

    /** Maps membership points onto the membership level band that contains them. */
    private static int levelFor(int points) {
        if (points >= 6) {
            return 4;
        }
        if (points >= 4) {
            return 3;
        }
        if (points >= 2) {
            return 2;
        }
        return 1;
    }
}
