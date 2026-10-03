package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's numeric {@code membershipLevel} on create. Starts at 1, adds 1
 * when an email address is present, adds 1 when {@code namesakeCount} is 0, and is
 * capped at 3. Level 4 is reserved for tenure. Runs after {@code namesakeCount} has
 * been counted and before the owner is saved, so the level is fixed at creation time.
 */
public class AssignMembershipLevel {

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
        if (level > 3) {
            level = 3;
        }
        owner.setMembershipLevel(level);
    }
}
