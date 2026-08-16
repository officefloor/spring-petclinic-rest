package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's numeric {@code membershipLevel} (1 to 3), scored at creation:
 * start at {@code 1}; add {@code 1} when an email is present; add {@code 1} when
 * {@code namesakeCount} is {@code 0}; capped at {@code 3} (level 4 is reserved for tenure).
 *
 * <p>Runs after {@link AssignNamesakeCount} (so the namesake count is set) and before
 * {@link SaveOwner}, so the level is persisted and available to the create audit line.
 */
public class AssignMembershipLevel {

    public void service(@Val Owner owner) {
        int level = 1;
        String email = owner.getEmail();
        if (email != null && !email.isBlank()) {
            level++;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            level++;
        }
        owner.setMembershipLevel(Math.min(level, 3));
    }
}
