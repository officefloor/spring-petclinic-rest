package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's numeric {@code membershipLevel} (1 to 3) at creation time.
 *
 * <p>The level starts at 1, gains 1 when the owner has an email address, and gains 1 when the
 * owner's {@code namesakeCount} is 0, then is capped at 3. (Level 4 is reserved for tenure.)
 *
 * <p>Runs in the {@code POST /api/owners} pipeline after {@link CountOwnerNamesakes} (so
 * {@code namesakeCount} is already frozen) and before {@link SaveOwner} (so the value is persisted)
 * and {@link AuditOwnerCreated} (so the audit line can record it). Frozen at creation and returned
 * unchanged on later reads.
 */
public class AssignOwnerMembershipLevel {

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
        owner.setMembershipLevel(Math.min(level, 3));
    }
}
