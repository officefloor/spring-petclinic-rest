package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners}: assigns the new owner's {@code membershipLevel} — a
 * number from 1 to 3 fixed at registration. It starts at 1, gains a level when the owner
 * has an email on file and another when the owner has no namesakes ({@code namesakeCount}
 * is 0), and is capped at 3 (level 4 is reserved for tenure). It runs after
 * {@link CountNamesakes} so the namesake count is known, and before {@link SaveOwner}
 * persists the owner and {@link AuditOwnerCreated} records the level. The value is stored
 * with the row and returned unchanged on later reads.
 */
public class AssignMembershipLevel {

    /** The highest level the registration rules award; level 4 is reserved for tenure. */
    private static final int MAX_LEVEL = 3;

    public void service(@Val Owner owner) {
        int level = 1;
        String email = owner.getEmail();
        if (email != null && !email.isBlank()) {
            level++;
        }
        Integer namesakeCount = owner.getNamesakeCount();
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        owner.setMembershipLevel(Math.min(level, MAX_LEVEL));
    }
}
