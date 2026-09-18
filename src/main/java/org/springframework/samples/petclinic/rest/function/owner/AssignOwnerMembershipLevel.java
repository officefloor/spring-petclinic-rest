package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs in {@code POST /api/owners} after the owner's namesake count has been recorded and before
 * it is saved, within the create transaction. Records on the built owner its numeric membership
 * level assigned at creation: it starts at 1, gains 1 when an email is present and 1 more when
 * {@link Owner#getNamesakeCount() namesakeCount} is 0, capped at 3 (level 4 is reserved for
 * tenure). Runs after {@link AssignOwnerNamesakeCount} so the count it reads is final.
 */
public class AssignOwnerMembershipLevel {

    /** The highest level assignable at creation; level 4 is reserved for tenure. */
    private static final int MAX_LEVEL = 3;

    public void service(@Val Owner owner) {
        int level = 1;
        boolean hasEmail = owner.getEmail() != null && !owner.getEmail().isEmpty();
        if (hasEmail) {
            level++;
        }
        boolean unique = owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0;
        if (unique) {
            level++;
        }
        owner.setMembershipLevel(Math.min(level, MAX_LEVEL));
    }
}
