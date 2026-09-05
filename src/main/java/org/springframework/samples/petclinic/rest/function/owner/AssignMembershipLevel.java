package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the numeric {@code membershipLevel} (1 to 3) at creation time. The level starts at
 * {@code 1}, gains {@code 1} when the owner has an email, gains a further {@code 1} when
 * {@code namesakeCount} is {@code 0}, and is capped at {@code 3} (level {@code 4} is reserved
 * for tenure). Runs after {@link CountNamesakes} so {@code namesakeCount} is already set.
 */
public class AssignMembershipLevel {

    public void service(@Val Owner owner) {
        int level = 1;
        if (owner.getEmail() != null && !owner.getEmail().isBlank()) {
            level++;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            level++;
        }
        owner.setMembershipLevel(Math.min(level, 3));
    }
}
